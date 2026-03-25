package project.team36.data.mcp

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Formål og funksjon:
 * Denne klienten håndterer den to-vegs kommunikasjonen med Meteorologisk
 * institutts (MET) nye MCP-server (Model Context Protocol). I stedet for vanlige HTTP-kall,
 * bruker jeg her Server-Sent Events (SSE) for å etablere en åpen lytte-strøm, og deretter
 * JSON-RPC for å sende kommandoer. Dette gir appen vår muligheten til å la AI-en oppdage
 * og bruke METs egne AI-verktøy dynamisk.
 *
 * Relevante filer:
 * - McpModels: Inneholder dataklassene jeg bruker for å strukturere JSON-RPC pakkene.
 */

// Jeg konfigurerer en egen JSON-parser her som er "lenient" (tilgivende).
// Siden MCP-protokoller ofte kan returnere litt ustrukturerte data eller ekstra felt
// vi ikke har definert i modellene våre, forhindrer dette at appen krasjer på parse-feil.
val mcpJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class MetMcpClient {

    // Jeg oppretter en standard OkHttp-klient som skal håndtere den underliggende nettverkstrafikken.
    private val client = OkHttpClient.Builder().build()

    // Dette er inngangsdøren til METs MCP-system (Server-Sent Events URL).
    private val sseUrl = "https://webapi.met.no/mcp-server/sse"

    // Funksjon 1: Opprette tilkobling
    // Jeg bruker 'suspendCancellableCoroutine' for å bygge en elegant bro mellom det asynkrone
    // callback-systemet til OkHttp SSE, og våre egne Kotlin Coroutines. Dette lar meg "pause"
    // koden til vi faktisk får et svar fra serveren.
    suspend fun connectAndGetEndpoint(): String = suspendCancellableCoroutine { continuation ->

        // Jeg bygger en forespørsel som ber om en kontinuerlig tekststrøm ("text/event-stream").
        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(client)

        // Jeg setter opp en lytter på strømmen.
        factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("MCP", "Koblet til MET SSE-strøm")
            }

            // Når MET sender et event av typen "endpoint", fanger jeg opp URL-en (data)
            // og sender den tilbake (resume) til coroutinen som venter.
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (type == "endpoint") {
                    Log.d("MCP", "Fikk endpoint fra MET: $data")
                    if (continuation.isActive) continuation.resume(data)
                }
            }

            // Sikkerhetsnett: Hvis forbindelsen brytes, kaster jeg en feil slik at appen kan håndtere det.
            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("MCP", "Feil på SSE-strømmen", t)
                if (continuation.isActive) {
                    continuation.resumeWithException(t ?: Exception("Ukjent nettverksfeil mot MET"))
                }
            }
        })
    }

    // Funksjon 2: Hente tilgjengelige verktøy
    // Jeg bruker endpointet jeg fikk fra funksjonen over, til å spørre MET hvilke AI-verktøy de tilbyr akkurat nå.
    suspend fun fetchTools(endpointUrl: String): List<McpTool> = suspendCancellableCoroutine { continuation ->

        // Jeg bygger en JSON-RPC-forespørsel spesifikt for metoden "tools/list".
        val requestBodyData = JsonRpcRequest(id = "1", method = "tools/list")
        val jsonString = mcpJson.encodeToString(JsonRpcRequest.serializer(), requestBodyData)
        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        // Dette er et vanlig POST-kall (ikke SSE) til endpointet vi fikk tildelt.
        val request = Request.Builder().url(endpointUrl).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        if (continuation.isActive) continuation.resumeWithException(Exception("Feilkode: ${response.code}"))
                        return
                    }
                    val responseBody = response.body?.string() ?: ""
                    try {
                        // Jeg dekoder det ytre JSON-RPC-svaret fra MET.
                        val rpcResponse = mcpJson.decodeFromString(JsonRpcResponse.serializer(), responseBody)
                        val resultJson = rpcResponse.result

                        if (resultJson != null) {
                            // FIKS: Jeg mater inn McpToolListResult.serializer() eksplisitt her for å
                            // sikre at parse-motoren vet nøyaktig hvordan den indre listen skal oversettes.
                            val toolListResult = mcpJson.decodeFromJsonElement(McpToolListResult.serializer(), resultJson)
                            if (continuation.isActive) continuation.resume(toolListResult.tools)
                        } else {
                            if (continuation.isActive) continuation.resume(emptyList())
                        }
                    } catch (e: Exception) {
                        Log.e("MCP", "Klarte ikke parse verktøy fra MET", e)
                        if (continuation.isActive) continuation.resumeWithException(e)
                    }
                }
            }
        })
    }

    // Funksjon 3: Utføre selve verktøyet
    // Dette er funksjonen jeg bruker for å faktisk kjøre et av METs verktøy (for eksempel hente værmelding for en koordinat).
    suspend fun callTool(
        endpointUrl: String,
        toolName: String,
        arguments: kotlinx.serialization.json.JsonElement
    ): String = suspendCancellableCoroutine { continuation ->

        // Jeg pakker inn verktøynavnet og argumentene (som AI-en har generert) i min McpCallToolParams-modell.
        val callParams = McpCallToolParams(name = toolName, arguments = arguments)
        val requestBodyData = JsonRpcRequest(
            id = "2",
            method = "tools/call", // Kommandoen for å be MET kjøre verktøyet.
            // FIKS: Sender inn McpCallToolParams.serializer() eksplisitt for riktig pakking av parameterne.
            params = mcpJson.encodeToJsonElement(McpCallToolParams.serializer(), callParams)
        )

        // Gjør det om til en JSON-streng og gjør den klar for nettverkstransport.
        val jsonString = mcpJson.encodeToString(JsonRpcRequest.serializer(), requestBodyData)
        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        val request = Request.Builder().url(endpointUrl).post(requestBody).build()

        // Jeg sender forespørselen.
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        if (continuation.isActive) continuation.resumeWithException(Exception("Feilkode fra MET: ${response.code}"))
                        return
                    }
                    val responseBody = response.body?.string() ?: ""
                    try {
                        // Jeg mottar dataene (f.eks. værmeldingen) og sjekker først om MET returnerte en feilkode.
                        val rpcResponse = mcpJson.decodeFromString(JsonRpcResponse.serializer(), responseBody)
                        if (rpcResponse.error != null) {
                            if (continuation.isActive) continuation.resume("Feil fra MET: ${rpcResponse.error.message}")
                            return
                        }

                        // Hvis alt gikk bra, trekker jeg ut rå-teksten fra resultatet og sender den tilbake til appen.
                        val resultString = rpcResponse.result?.toString() ?: "Ingen data returnert fra MET."
                        if (continuation.isActive) continuation.resume(resultString)
                    } catch (e: Exception) {
                        Log.e("MCP", "Klarte ikke parse verktøy-resultat fra MET", e)
                        if (continuation.isActive) continuation.resumeWithException(e)
                    }
                }
            }
        })
    }
}