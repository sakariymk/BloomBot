package project.team36.data.ai

import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Formål og funksjon:
 * Her har jeg definert kjernenettet vårt for kommunikasjon med Anthropics servere (Claude).
 * Dette er nettverkskontrakten som setter reglene for hvordan appen min skal koble seg til AI-en
 * og sende over våre system-prompter, bruker-spørsmål og verktøy.
 *
 * Relevante filer:
 * - MainActivity: Hvor jeg bygger opp Retrofit-motoren med riktige timeouts.
 * - HybridAiRepository: Hvor jeg orkestrerer selve samtalen med Claude.
 * - ClaudeModels: Hvor jeg har definert datastrukturen jeg sender og mottar.
 */

// Igjen lar jeg Retrofit ta seg av det tunge løftet ved å definere et interface.
interface ClaudeApi {

    // Jeg må sende med disse tre faste headerne på hvert eneste anrop, ellers avviser Anthropic forespørselen.
    @Headers(
        "Accept: application/json",           // Jeg forventer JSON-svar tilbake.
        "Content-Type: application/json",     // Jeg sender dataene mine i JSON-format.
        "anthropic-version: 2023-06-01"       // Jeg spesifiserer API-protokollen deres (dette er et strengt Anthropic-krav).
    )

    // Jeg bruker et POST-kall fordi jeg aktivt sender en pakke med data (meldinger) opp til AI-serveren.
    @POST("v1/messages")

    // Kjøres i bakgrunnen (suspend) for å holde appen min responsiv mens Claude "tenker".
    suspend fun sendMessage(

        // Jeg injiserer den hemmelige API-nøkkelen vår dynamisk i headeren for dette spesifikke kallet.
        @Header("x-api-key") apiKey: String,

        // Her legger jeg hele datamodellen min (requesten) i kroppen på POST-meldingen.
        @Body request: ClaudeRequest

        // Jeg har strukturert koden slik at den automatisk oversetter JSON-svaret tilbake til et ClaudeResponse-objekt jeg kan jobbe med.
    ): ClaudeResponse
}