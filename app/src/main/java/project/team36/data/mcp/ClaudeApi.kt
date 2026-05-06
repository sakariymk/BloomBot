package project.team36.data.ai

import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Formål og funksjon:
 * Her har vi definert kjernenettet vårt for kommunikasjon med Anthropics servere (Claude).
 * Dette er nettverkskontrakten som setter reglene for hvordan appen vårskal koble seg til AI-en
 * og sende over våre system-prompter, bruker-spørsmål og verktøy.
 *
 * Relevante filer:
 * - MainActivity: Hvor vi bygger opp Retrofit-motoren med riktige timeouts.
 * - HybridAiRepository: Hvor vi orkestrerer selve samtalen med Claude.
 * - ClaudeModels: Hvor vi har definert datastrukturen vi sender og mottar.
 */

// vi bruker et interface her fordi Retrofit krever det for å automatisk generere den underliggende nettverkskoden for oss.
interface ClaudeApi {

    // Anthropic API-et er veldig strengt på hvilke headers som sendes inn.
    // vi må spesifisere versjon og at all kommunikasjon skjer vi a JSON, ellers vi l serveren avvise forespørselen vår.
    @Headers(
        "Accept: application/json",           // Forteller serveren at vi forventer å få svaret tilbake i JSON-format.
        "Content-Type: application/json",     // Forteller serveren at dataene vi sender oppover også er formatert som JSON.
        "anthropic-version: 2023-06-01"       // Dette er et strengt krav fra Anthropic for å definere hvilken API-protokoll vi bruker.
    )

    // Siden vi sender opp en relativt stor datapakke (prompt, logg og verktøy), må vi bruke et HTTP POST-kall.
    // "v1/messages" er det spesifikke endepunktet hos Claude for å generere tekstbaserte svar.
    @POST("v1/messages")

    // Funksjonen er 'suspend' slik at nettverkskallet kjører asynkront i bakgrunnen (i en coroutine).
    // På den måten unngår vi at selve appen og brukergrensesnittet fryser mens Claude "tenker".
    suspend fun sendMessage(

        // API-nøkkelen vår injiseres dynamisk i headeren for dette spesifikke kallet.
        // Dette gjøres vi a @Header-annotasjonen slik at den blir gjemt i HTTP-forespørselen som "x-api-key".
        @Header("x-api-key") apiKey: String,

        // Her legger vi hele datamodellen vår (requesten med system prompt og samtalelogg) i selve "kroppen" av POST-meldingen.
        // Retrofit vi l automatisk gjøre om Kotlin-objektet ClaudeRequest til en JSON-streng før det sendes.
        @Body request: ClaudeRequest

        // Når API-kallet er ferdig, sørger Retrofit og Kotlin Serialization for å automatisk
        // oversette JSON-svaret fra Claude tilbake til et ClaudeResponse-objekt som vi enkelt kan hente data fra.
    ): ClaudeResponse
}