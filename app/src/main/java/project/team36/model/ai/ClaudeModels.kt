// Jeg skrur på eksperimentelle funksjoner her for å kunne bruke @EncodeDefault, som er avgjørende for JSON-formateringen min.
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package project.team36.model.ai

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Formål og funksjon:
 * Dette er "ordboken" jeg har bygget for å oversette appens interne Kotlin-klasser til den
 * krevende JSON-strukturen Claude 4.6 forventer. Jeg har lagt inn betydelig innsats her for å sikre
 * at tomme felter strippes vekk, siden Claude avviser pakker med overflødig eller tom data (Error 400).
 *
 * Relevante filer:
 * - ClaudeApi: Bruker disse modellene som @Body i nettverkskallene.
 * - HybridAiRepository: Hvor jeg faktisk fyller disse data-klassene med samtaleloggen og verktøyene.
 */

// Jeg bruker @Serializable på alle klasser slik at Kotlin vet hvordan de skal gjøres om til tekst for internett.
@Serializable
data class ClaudeRequest(
    // Jeg hardkoder modellen vi bruker.
    val model: String,

    // Jeg har satt en grense på 1024 tokens for å holde API-kostnadene våre nede per kall.
    val max_tokens: Int = 1024,

    // @EncodeDefault(Mode.NEVER) er et viktig sikkerhetsnett jeg la inn. Hvis jeg ikke sender med
    // en systemprompt, sørger denne for at hele feltet slettes fra JSON-pakken i stedet for å sende 'null'.
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val system: String? = null,

    // Listen over selve samtalen. Denne må alltid være med.
    val messages: List<ClaudeMessage>,

    // Listen over verktøy (f.eks. værsjekken vår). Sendes kun hvis jeg faktisk legger ved verktøy.
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val tools: List<ClaudeTool>? = null
)

// Definerer strukturen for en enkelt melding (hvem som snakker, og hva de sier).
@Serializable
data class ClaudeMessage(
    val role: String,
    val content: List<ContentBlock>
)

// Dette er min mest fleksible byggekloss. Siden en melding både kan være tekst,
// et verktøy-kall, eller et verktøy-svar, har jeg gjort nesten alle feltene valgfrie.
@Serializable
data class ContentBlock(
    val type: String,

    // Resten av disse feltene slettes automatisk fra JSON-pakken hvis de ikke brukes.
    @EncodeDefault(EncodeDefault.Mode.NEVER) val text: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val name: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val input: JsonElement? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val tool_use_id: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val content: String? = null
)

// Mine definisjoner for hvordan verktøyene vi tilbyr AI-en skal se ut.
@Serializable
data class ClaudeTool(
    val name: String,
    val description: String,
    val input_schema: ToolInputSchema
)

// Dette forteller Claude nøyaktig hvilke variabler jeg krever (f.eks. lat/lon) når han vil bruke et verktøy.
@Serializable
data class ToolInputSchema(
    val type: String,
    val properties: Map<String, PropertyDefinition>,
    val required: List<String>
)

// Beskrivelse av selve parameterne til verktøyene.
@Serializable
data class PropertyDefinition(
    val type: String,
    val description: String
)

// Slik jeg forventer at Anthropic pakker inn svaret de sender tilbake til meg.
@Serializable
data class ClaudeResponse(
    val content: List<ContentBlock> = emptyList(),

    // Jeg sjekker denne for å se om Claude stoppet fordi han var ferdig ("end_turn"),
    // eller fordi han vil at jeg skal kjøre en funksjon for ham ("tool_use").
    val stop_reason: String? = null
)