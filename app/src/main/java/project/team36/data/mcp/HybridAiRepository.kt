package project.team36.data.ai

import kotlinx.serialization.json.*
import project.team36.data.klima.MetWeatherApi
import project.team36.data.klima.frost.getNearestStation
import project.team36.data.klima.frost.getTemperatures
import project.team36.data.mcp.MetMcpClient
import project.team36.data.plant.PlantDataSource
import project.team36.ui.map.MapViewModel
import project.team36.model.ai.*
import retrofit2.HttpException
class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    private val plantDataSource: PlantDataSource,
    private val frostClientId: String,
    private val claudeApiKey: String
) {
    private val frostTool = ClaudeTool(
        name = "get_current_frost_status",
        description = "Henter nåværende temperatur for å sjekke om det er frostfare akkurat nå.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = mapOf(
                "latitude" to PropertyDefinition("number", "Breddegrad"),
                "longitude" to PropertyDefinition("number", "Lengdegrad")
            ),
            required = listOf("latitude", "longitude")
        )
    )

    private val weatherTool = ClaudeTool(
        name = "weatherforecast",
        description = "Henter værmeldingen for de neste dagene.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = mapOf(
                "latitude" to PropertyDefinition("number", "Breddegrad"),
                "longitude" to PropertyDefinition("number", "Lengdegrad")
            ),
            required = listOf("latitude", "longitude")
        )
    )

    // Nytt: alert-verktøy uten påkrevde parametere
    private val alertTool = ClaudeTool(
        name = "get_weather_alerts",
        description = "Henter aktive ekstremværvarsler fra Meteorologisk institutt for Norge. Bruk dette for å advare brukeren om farlige værforhold.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        )
    )

    suspend fun getPlantRecommendations(lat: Double, lon: Double): String {
        // Sørg for at MCP-tilkoblingen er oppe før vi starter
        try {
            mcpClient.connect()
        } catch (e: Exception) {
            return "Kunne ikke koble til værtjenesten: ${e.message}"
        }

        val plants = try { plantDataSource.fetchPlants() } catch (e: Exception) { emptyList() }

        val plantsText = plants.take(15).joinToString("\n") {
            "- ${it.name}: Sone ${it.climateZone}, Frostsikker: ${it.isFrostResistant}"
        }

        val systemPrompt = """
            Du er en hageekspert i Norge. Brukeren er på lokasjonen Lat: $lat, Lon: $lon.
            Du SKAL bruke verktøyene dine for å sjekke vær, frost og aktive varsler før du gir råd.
            Hvis det er aktive ekstremværvarsler, skal du nevne disse tydelig først.
            Baser rådene dine på disse plantene:
            $plantsText
            Svar kort og vennlig på norsk.
        """.trimIndent()

        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen nå?")))
        )

        return try {
            var response = claudeApi.sendMessage(
                apiKey = claudeApiKey,
                request = ClaudeRequest(
                    model = "claude-sonnet-4-5",
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(frostTool, weatherTool, alertTool) // alertTool lagt til
                )
            )

            // Loop for å håndtere flere tool_use-runder (Claude kan kalle flere verktøy)
            while (response.stop_reason == "tool_use") {
                conversation.add(ClaudeMessage("assistant", response.content))

                val toolUseBlocks = response.content.filter { it.type == "tool_use" }
                val toolResultBlocks = mutableListOf<ContentBlock>()

                for (block in toolUseBlocks) {
                    val toolId = block.id ?: continue
                    val toolName = block.name ?: ""
                    val inputJson = block.input?.jsonObject
                    val rLat = inputJson?.get("latitude")?.jsonPrimitive?.double ?: lat
                    val rLon = inputJson?.get("longitude")?.jsonPrimitive?.double ?: lon

                    val resultText = when (toolName) {
                        "get_current_frost_status" -> {
                            val station = getNearestStation(rLat, rLon, frostClientId)
                            if (station != null)
                                "Temp på ${station.name}: ${getTemperatures(station.id, frostClientId)}°C"
                            else
                                "Ingen stasjon funnet."
                        }
                        "weatherforecast" -> mcpClient.getForecast(rLat, rLon)
                        "get_weather_alerts" -> mcpClient.getAlerts() // Nytt
                        else -> "Ukjent verktøy: $toolName"
                    }

                    toolResultBlocks.add(
                        ContentBlock(
                            type = "tool_result",
                            tool_use_id = toolId,
                            content = resultText
                        )
                    )
                }

                conversation.add(ClaudeMessage("user", toolResultBlocks))

                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = "claude-sonnet-4-5",
                        system = systemPrompt,
                        messages = conversation
                    )
                )
            }

            response.content.firstOrNull { it.type == "text" }?.text
                ?: "AI-en ga et svar uten tekst."

        } catch (e: HttpException) {
            "AI-feil: ${e.code()} - ${e.response()?.errorBody()?.string()}"
        } catch (e: Exception) {
            "Feil: ${e.message}"
        }
    }
}