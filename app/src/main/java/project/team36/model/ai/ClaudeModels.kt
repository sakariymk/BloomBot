// vi  skrur på eksperimentelle funksjoner her for å kunne bruke @EncodeDefault, som er avgjørende for JSON-formateringen vår.
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package project.team36.model.ai

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Formål og funksjon:
 * Dette er "ordboken" vi  har bygget for å oversette appens interne Kotlin-klasser til den
 * krevende JSON-strukturen Claude forventer. vi  har lagt inn betydelig innsats her for å sikre
 * at tomme felter strippes vekk, siden Claude avviser pakker med overflødig eller tom data (Error 400).
 *
 * Relevante filer:
 * - ClaudeApi: Bruker disse modellene som @Body i nettverkskallene.
 * - HybridAiRepository: Hvor vi  faktisk fyller disse data-klassene med samtaleloggen og verktøyene.
 */

// vi  bruker @Serializable på alle klasser slik at Kotlin (via kotlinx.serialization) vet nøyaktig hvordan de skal gjøres om til JSON-tekst for internett.
@Serializable
data class ClaudeRequest(
    // Definerer hvilken versjon av Claude vi snakker med (f.eks. claude-3-5-sonnet).
    val model: String,

    // vi  har satt en grense på 1024 tokens for å holde API-kostnadene våre nede per kall, 
    // og for å sikre at AI-en ikke genererer uendelig lange svar.
    val max_tokens: Int = 1024,

    // @EncodeDefault(Mode.NEVER) er et viktig sikkerhetsnett vi  la inn. Hvis vi ikke sender med
    // en systemprompt, sørger denne linjen for at hele feltet slettes fullstendig fra JSON-pakken i stedet for å sende '"system": null'. 
    // Anthropic API-et krasjer nemlig hvis det mottar eksplisitte null-verdier her.
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val system: String? = null,

    // Listen over selve samtalen (bruker- og assistent-meldinger). Denne er obligatorisk for API-et.
    val messages: List<ClaudeMessage>,

    // Listen over verktøy (f.eks. værsjekken vår). Sendes kun hvis vi faktisk har lagt ved noen verktøy i requesten.
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val tools: List<ClaudeTool>? = null
)

// Definerer strukturen for en enkelt melding i samtaleloggen.
@Serializable
data class ClaudeMessage(
    // 'role' forteller API-et hvem som snakker. Denne vil alltid være enten "user" eller "assistant".
    val role: String,

    // Innholdet i meldingen. Siden Claude støtter multimodalitet og verktøy, er innholdet en liste med blokker i stedet for bare en enkel tekststreng.
    val content: List<ContentBlock>
)

// Dette er vår mest fleksible byggekloss for å håndtere Anthropics komplekse API uten å måtte lage mange forskjellige klasser.
// En ContentBlock kan representere tre helt forskjellige ting: ren tekst, et verktøy-kall fra AI-en, eller resultatet fra et verktøy vi sender tilbake.
@Serializable
data class ContentBlock(
    // 'type' definerer hva denne blokken er (f.eks. "text", "tool_use", eller "tool_result").
    val type: String,

    // Siden denne dataklassen brukes til tre forskjellige formål, er resten av feltene valgfrie (nullable).
    // Takket være EncodeDefault.Mode.NEVER vil kun de feltene som faktisk får en verdi bli med i den endelige JSON-pakken.
    // Alt annet strippes bort, noe som holder pakken liten og gyldig.

    @EncodeDefault(EncodeDefault.Mode.NEVER) val text: String? = null,           // Brukes når typen er "text"
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,             // Brukes når typen er "tool_use" (unik ID for kallet)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val name: String? = null,           // Brukes når typen er "tool_use" (navnet på verktøyet)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val input: JsonElement? = null,     // Brukes når typen er "tool_use" (parametrene AI-en sender inn, f.eks. lat/lon)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val tool_use_id: String? = null,    // Brukes når typen er "tool_result" (refererer tilbake til ID-en AI-en genererte)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val content: String? = null         // Brukes når typen er "tool_result" (selve værdataen vi sender tilbake)
)

// Definisjon for hvordan vi beskriver verktøyene vi tilbyr AI-en (sendes med i ClaudeRequest).
@Serializable
data class ClaudeTool(
    val name: String,             // Navnet AI-en må bruke for å kalle verktøyet (f.eks. "weatherforecast")
    val description: String,      // Forklaring til AI-en om hva verktøyet gjør, slik at den vet NÅR den skal bruke det.
    val input_schema: ToolInputSchema // Definerer hvilke variabler AI-en må sende til oss for at verktøyet skal virke.
)

// JSON Schema-oppsett som forteller Claude nøyaktig hvordan parameterne til verktøyet skal struktureres.
@Serializable
data class ToolInputSchema(
    val type: String,                               // Alltid "object" for Anthropic-verktøy.
    val properties: Map<String, PropertyDefinition>, // En map med parameter-navn (f.eks. "latitude") og definisjonen av dem.
    val required: List<String>                      // En liste over hvilke parametere AI-en MÅ fylle ut.
)

// Beskriver en enkelt parameter for et verktøy.
@Serializable
data class PropertyDefinition(
    val type: String,          // Datatypen, for eksempel "number" eller "string".
    val description: String    // Hjelpetekst til AI-en, f.eks. "Breddegrad i desimalformat".
)

// Slik vi forventer at Anthropic pakker inn svaret de sender tilbake til appen vår.
@Serializable
data class ClaudeResponse(
    // Listen med innholdblokker AI-en genererte. Kan være tekst-svar, eller en "tool_use"-blokk hvis den vil sjekke været.
    val content: List<ContentBlock> = emptyList(),

    // Dette er den absolutt viktigste variabelen for logikken vår i HybridAiRepository.
    // Hvis denne er "end_turn", er AI-en ferdig og vi kan vise teksten til brukeren.
    // Hvis denne er "tool_use", betyr det at AI-en venter på at vi skal hente værdata og sende det tilbake til den.
    val stop_reason: String? = null
)