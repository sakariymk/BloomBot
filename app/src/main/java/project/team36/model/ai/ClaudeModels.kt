// vi  skrur på eksperimentelle funksjoner her for å kunne bruke @EncodeDefault, som er avgjørende for JSON-formateringen vår.
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package project.team36.model.ai

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


// Outlines details about our claude request (model, tokens, tools)
@Serializable
data class ClaudeRequest(

    val model: String,

    val max_tokens: Int = 1024,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val system: String? = null,

    val messages: List<ClaudeMessage>,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val tools: List<ClaudeTool>? = null
)

// Defines structure of an individual message
@Serializable
data class ClaudeMessage(
    val role: String,
    val content: List<ContentBlock>
)


@Serializable
data class ContentBlock(
    val type: String,

    @EncodeDefault(EncodeDefault.Mode.NEVER) val text: String? = null,           // Brukes når typen er "text"
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,             // Brukes når typen er "tool_use" (unik ID for kallet)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val name: String? = null,           // Brukes når typen er "tool_use" (navnet på verktøyet)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val input: JsonElement? = null,     // Brukes når typen er "tool_use" (parametrene AI-en sender inn, f.eks. lat/lon)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val tool_use_id: String? = null,    // Brukes når typen er "tool_result" (refererer tilbake til ID-en AI-en genererte)
    @EncodeDefault(EncodeDefault.Mode.NEVER) val content: String? = null         // Brukes når typen er "tool_result" (selve værdataen vi sender tilbake)
)

// Definerer verktøybruk
@Serializable
data class ClaudeTool(
    val name: String,
    val description: String,
    val input_schema: ToolInputSchema
)

// JSON schema-setup
@Serializable
data class ToolInputSchema(
    val type: String,
    val properties: Map<String, PropertyDefinition>,
    val required: List<String>
)

// Describes perametres for a tool
@Serializable
data class PropertyDefinition(
    val type: String,
    val description: String
)

// Expected response format
@Serializable
data class ClaudeResponse(

    val content: List<ContentBlock> = emptyList(),
    val stop_reason: String? = null
)