package project.team36.data.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Formål og funksjon:
 * Disse dataklassene håndterer den strenge JSON-RPC 2.0-standarden
 * som kreves for å snakke med METs nye Model Context Protocol (MCP) server.
 * Dette er selve "ordboken" vår: den sikrer at appen min automatisk kan oversette
 * våre interne Kotlin-objekter til det eksakte tekstformatet som AI-serveren forventer.
 *
 * Relevante filer:
 * - MetMcpClient: Klienten jeg har bygget som utelukkende bruker disse modellene
 *   for å strukturere all nettverkstrafikk inn og ut.
 */

// 1. Kjerne-protokollen (Forespørsler)
// Dette er "konvolutten" jeg bruker for å sende kommandoer til MET MCP-serveren.
@Serializable
data class JsonRpcRequest(
    // Standarden for MCP krever at jeg alltid spesifiserer versjonen av RPC-protokollen.
    // Jeg har satt "2.0" som standardverdi slik at vi aldri glemmer den.
    val jsonrpc: String = "2.0",

    // Jeg genererer en unik ID for hver melding. Dette er kritisk for at jeg skal
    // kunne koble riktig svar fra serveren til riktig spørsmål fra appen.
    val id: String,

    // Hvilken handling jeg vil at serveren skal utføre (f.eks. "tools/list" eller "tools/call").
    val method: String,

    // Her sender jeg med valgfrie parametere (som koordinater). Jeg bruker JsonElement
    // fordi parameterne kan ha vidt forskjellig struktur avhengig av hvilket verktøy jeg kaller.
    val params: JsonElement? = null
)

// 2. Kjerne-protokollen (Svar)
// Dette er strukturen jeg forventer at MET MCP-serveren bruker når den svarer meg.
@Serializable
data class JsonRpcResponse(
    // Serveren bekrefter hvilken protokollversjon vi snakker.
    val jsonrpc: String,

    // Returnerer ID-en min, slik at jeg vet at dette er svaret på akkurat min forespørsel.
    val id: String,

    // Hvis alt gikk bra, ligger selve innholdet her. Igjen bruker jeg JsonElement
    // for maksimal fleksibilitet før jeg bestemmer meg for hvordan jeg skal pakke det ut.
    val result: JsonElement? = null,

    // Jeg har bygget inn et dedikert feil-objekt, slik at appen min trygt kan fange opp
    // og håndtere situasjoner der noe går galt på METs side.
    val error: JsonRpcError? = null
)

// Standardisert feilhåndtering i JSON-RPC. Gir meg nøyaktig feilkode og en lesbar feilmelding.
@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String
)

// 3. Modeller for Verktøyene (Tools) fra MET
// Siden METs MCP-server kan oppdatere verktøyene sine dynamisk,
// bruker jeg denne klassen for å pakke ut listen over hva serveren kan gjøre akkurat nå.
@Serializable
data class McpToolListResult(
    val tools: List<McpTool>
)

// Strukturen på et enkelt AI-verktøy (for eksempel "weatherforecast").
@Serializable
data class McpTool(
    // Hva verktøyet heter.
    val name: String,

    // En beskrivelse av hva det gjør (som AI-en leser for å forstå når den skal bruke det).
    val description: String,

    // Et JSON Schema som forklarer nøyaktig hvilke variabler (f.eks. lat/lon)
    // verktøyet krever. Jeg beholder det som JsonElement for å unngå krasj på komplekse skjemaer.
    val inputSchema: JsonElement
)

// 4. Utførelse av verktøy
// Denne modellen bruker jeg i det øyeblikket jeg faktisk ber MET om å kjøre et av verktøyene sine.
@Serializable
data class McpCallToolParams(
    // Navnet på verktøyet jeg vil at serveren skal aktivere.
    val name: String,

    // Argumentene verktøyet krever (f.eks. koordinatene). Dette oversettes
    // trygt til et raw JsonElement før det skytes over nettverket.
    val arguments: JsonElement
)