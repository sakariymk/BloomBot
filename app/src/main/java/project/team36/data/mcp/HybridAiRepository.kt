package project.team36.data.ai

import kotlinx.serialization.json.*
// <------ ENDRING: Importerte '.first()' for å kunne lese plantene fra databasen
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import project.team36.data.mcp.MetMcpClient
// <------ ENDRING: Endret fra PlantDataSource til LocationRepository og SavedLocation
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.ai.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import android.util.Log
import retrofit2.HttpException

class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    // <------ ENDRING: Denne heter nå LocationRepository, fordi vi bruker Room Database
    private val plantDataSource: LocationRepository,
    private val claudeApiKey: String
) {

    private val weatherTool = ClaudeTool(
        name = "weatherforecast",
        description = "Henter værinformasjon for i dag de kommende dagene.",
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

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // <------ ENDRING: Ny funksjon for å hente RAW data for testing
    suspend fun getRawApiTest(location: SavedLocation): String {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0
        val builder = java.lang.StringBuilder()

        builder.append("\n=== MET WEATHER API (Via MCP) ===\n")
        try {
            // Her henter vi selve råteksten fra MCP-serveren (før Claude ser den)
            val rawMet = mcpClient.getForecast(lat, lon)
            builder.append(rawMet)
        } catch (e: Exception) {
            builder.append("Feil ved MET API: ${e.message}\n")
        }

        return builder.toString()
    }

    // <------ ENDRING: Endret funksjonen til å ta imot hele SavedLocation i stedet for to Double-verdier
    suspend fun getPlantRecommendations(location: SavedLocation): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0

        val plants = try { plantDataSource.getPlantsForLocation(location.id).first() } catch (e: Exception) { emptyList() }

        val plantsText = if (plants.isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            plants.take(15).joinToString("\n") {
                "- ${it.name}: Sone: ${it.climateZone}, Lysbehov: ${it.light}, Vann Per Uke: ${it.waterPerWeek}, Frostsikker: ${it.isFrostResistant}"
            }
        }

        // <------ ENDRING: Ledeteksten tvinger Claude til å svare i JSON
        val systemPrompt = """
            Du er en profesjonell og velformulert hageekspert i Norge som skal gi råd til en hobbygartner/bruker.
            Du får dagens værinformasjon fra en API som heter MetWeather, du får følgende koordinater Lat: $lat, Lon: $lon.
            Du får informasjon om relevante planter på koordinatene, basert på disse plantene:  $plantsText.
            
            Skriv først dagens vær på formen:
            Temperatur:
            Nedbør:
            Solskinnstimer:
            Vind:
            
            VIKTIG REGEL for følgende repons: Du MÅ returnere svaret ditt UTELUKKENDE som et gyldig JSON-objekt. 
            Ikke skriv noe annen tekst, ingen introduksjon, ingen markdown-blokker, bare ren JSON som matcher nøyaktig denne strukturen:
            {
              "weatherSummary": "Oppsummering av været for dagen og evt. ekstremværvarsler",
              "plantAdvice": [
                { "plantName": "Plantenavn", "advice": "Spesifikt råd for denne planten basert på været" }
              ],
              "generalTasks": ["Generell oppgave 1", "Generell oppgave 2"]
            }
        """.trimIndent()

        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen nå? Svar KUN med JSON.")))
        )

        return try {
            var response = claudeApi.sendMessage(
                apiKey = claudeApiKey,
                request = ClaudeRequest(
                    model = "claude-sonnet-4-5",
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(weatherTool, alertTool)
                )
            )

            var toolLoops = 0
            while (response.stop_reason == "tool_use" && toolLoops < 3) {
                toolLoops++
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
                        "weatherforecast" -> {
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val forecast = weatherRepo.getParsedForecast(rLat, rLon)
                            "Værvarsel: ${forecast?.temperature}°C, ${forecast?.weatherSymbol}, Vind: ${forecast?.windSpeed} m/s, Nedbør: ${forecast?.precipitation} mm."
                        }
                        else -> "Ukjent verktøy."
                    }

                    toolResultBlocks.add(
                        ContentBlock(type = "tool_result", tool_use_id = toolId, content = resultText)
                    )
                }

                conversation.add(ClaudeMessage("user", toolResultBlocks))

                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = "claude-sonnet-4-5",
                        system = systemPrompt,
                        messages = conversation,
                        tools = listOf(weatherTool, alertTool)
                    )
                )
            }

            // <------ ENDRING: Ekstraher teksten, rens bort evt. markdown (```json ... ```) og parse til objekt
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null
            val cleanJson = rawText.replace(Regex("```json|```"), "").trim()

            jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)

        } catch (e: Exception) {
            Log.e("AI_ERROR", "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }

    // <------ ENDRING: Returtypen er AiRecommendationResponse?
    suspend fun getFuturePlantRecommendations(location: SavedLocation): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0

        val plants = try { plantDataSource.getPlantsForLocation(location.id).first() } catch (e: Exception) { emptyList() }

        val plantsText = if (plants.isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            plants.take(15).joinToString("\n") {
                "- ${it.name}: Sone ${it.climateZone}, Frostsikker: ${it.isFrostResistant}, VannperUke: ${it.waterPerWeek}"
            }
        }

        // <------ ENDRING: Stram JSON-prompt også for fremtidsvarsler
        val systemPrompt = """
            Du er en profesjonell og velformulert hageekspert i Norge som skal gi råd til en hobbygartner/bruker.
            Du får dagens og kommende dagenes værinformasjon fra en API som heter MetWeather, du får følgende koordinater Lat: $lat, Lon: $lon.
            Du får informasjon om relevante planter på koordinatene, basert på disse plantene:  $plantsText.
            
            Skriv først dagens vær på formen:
            Temperatur:
            Nedbør:
            Solskinnstimer:
            Vind:
            
            Så for de kommende et par antall dager på formen:
            Temperatur:
            Nedbør:
            Solskinnstimer:
            Vind:
            
            VIKTIG REGEL for følgende respons: Du MÅ returnere svaret ditt UTELUKKENDE som et gyldig JSON-objekt. 
            Ikke skriv noe annen tekst, ingen introduksjon, ingen markdown-blokker, bare ren JSON som matcher nøyaktig denne strukturen:
            {
              "weatherSummary": "Oppsummering av været for de kommende dagene",
              "plantAdvice": [
                { "plantName": "Plantenavn", "advice": "Spesifikt fremtidsråd for denne planten basert på været" }
              ],
              "generalTasks": ["Fremtidig oppgave 1", "Fremtidig oppgave 2"]
            }
        """.trimIndent()

        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen fremover? Svar KUN med JSON.")))
        )

        return try {
            var response = claudeApi.sendMessage(
                apiKey = claudeApiKey,
                request = ClaudeRequest(
                    model = "claude-sonnet-4-5",
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(weatherTool)
                )
            )

            var toolLoops = 0
            while (response.stop_reason == "tool_use" && toolLoops < 3) {
                toolLoops++
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
                        "weatherforecast" -> {
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val forecast = weatherRepo.getParsedForecast(rLat, rLon)
                            "Værvarsel: ${forecast?.temperature}°C, ${forecast?.weatherSymbol}, Vind: ${forecast?.windSpeed} m/s, Nedbør: ${forecast?.precipitation} mm."
                        }
                        else -> "Ukjent verktøy."
                    }

                    toolResultBlocks.add(
                        ContentBlock(type = "tool_result", tool_use_id = toolId, content = resultText)
                    )
                }

                conversation.add(ClaudeMessage("user", toolResultBlocks))

                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = "claude-sonnet-4-5",
                        system = systemPrompt,
                        messages = conversation,
                        tools = listOf(weatherTool)
                    )
                )
            }

            // <------ ENDRING: Ekstraher og parse JSON for fremtidsvarsel
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null
            val cleanJson = rawText.replace(Regex("```json|```"), "").trim()

            jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)

        } catch (e: Exception) {
            Log.e("AI_ERROR", "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }
}