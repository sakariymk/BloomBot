package project.team36.data.ai

import kotlinx.serialization.json.*
// <------ ENDRING: Importerte '.first()' for å kunne lese plantene fra databasen
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import project.team36.data.klima.MetWeatherApi
import project.team36.data.klima.frost.getNearestStation
import project.team36.data.klima.frost.getTemperatures
import project.team36.data.mcp.MetMcpClient
// <------ ENDRING: Endret fra PlantDataSource til LocationRepository og SavedLocation
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.ui.map.MapViewModel
import project.team36.model.ai.*
import retrofit2.HttpException

class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    // <------ ENDRING: Denne heter nå LocationRepository, fordi vi bruker Room Database
    private val plantDataSource: LocationRepository,
    private val frostClientId: String,
    private val claudeApiKey: String
) {
    private val frostTool = ClaudeTool(
        name = "get_current_frost_status",
        description = "Henter nåværende vær.",
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

    // <------ ENDRING: Ny funksjon for å hente RAW data for testing
    suspend fun getRawApiTest(location: SavedLocation): String {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0
        val builder = java.lang.StringBuilder()

        builder.append("=== FROST API ===\n")
        try {
            val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).toString()

            // Bruker din nye FrostRepository!
            val frostRepo = project.team36.data.klima.frost.FrostRepository()
            val weatherData = frostRepo.getFullWeatherForLocation(lat, lon, currentTime)

            if (weatherData != null) {
                builder.append("Stasjon ID: ${weatherData.stationId}\n")
                builder.append("Nåværende Temperatur: ${weatherData.temperature} °C\n")
                builder.append("Nedbør (i dag): ${weatherData.precipitation} mm\n")
                builder.append("Soltimer (i dag): ${weatherData.sunshineHours}\n")
            } else {
                builder.append("Ingen Frost-stasjon funnet i nærheten.\n")
            }
        } catch (e: Exception) {
            builder.append("Feil ved Frost API: ${e.message}\n")
        }

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
    suspend fun getPlantRecommendations(location: SavedLocation): String {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0

        // <------ ENDRING: Henter fra database basert på ID, og bruker .first()
        val plants = try { plantDataSource.getPlantsForLocation(location.id).first() } catch (e: Exception) { emptyList() }

        // <------ ENDRING: Sjekker om listen er tom, og oppdaterer meldingen basert på databasen
        val plantsText = if (plants.isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            plants.take(15).joinToString("\n") {
                "- ${it.name}: Sone: ${it.climateZone}, Lysbehov: ${it.light}, Vann Per Uke: ${it.waterPerWeek}, Frostsikker: ${it.isFrostResistant}"
            }
        }
        val systemPrompt = """
            Du er en profesjonell og velformulert hageekspert i Norge som skal gi råd til en hobbygartner/bruker.
            Du får værinformasjon fra en API som heter FROST, du får følgende koordinater Lat: $lat, Lon: $lon.
            Du får informasjon om relevante planter på koordinatene.
            Du SKAL bruke verktøyene dine for å sjekke nåværende vær som du får fra FROST API tool'en før du gir råd.
            Skriv også opp værinformasjonen du får for dagen slik at man får en oversikt.
            Hvis det er aktive ekstremværvarsler, skal du nevne disse tydelig først.
            Baser rådene dine på disse plantene:
            $plantsText
            Svar konkret, strukturert og på Norsk.
        """.trimIndent()

        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre basert på informasjonen, plantene, været? Vis været og anbefalinger.")))
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

            // <------ ENDRING: Endret fra 'if' til 'while' for at Claude kan bruke flere verktøy etter hverandre
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
                        "get_current_frost_status" -> {
                            // <------ ENDRING: Bruker den nye FrostRepository for å gi AI-en temp, nedbør og sol!
                            val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).toString()
                            val frostRepo = project.team36.data.klima.frost.FrostRepository()
                            val weatherData = frostRepo.getFullWeatherForLocation(rLat, rLon, currentTime)

                            if (weatherData != null) {
                                "Stasjon ID: ${weatherData.stationId}. Temp: ${weatherData.temperature}°C, Nedbør (i dag): ${weatherData.precipitation} mm, Soltimer (i dag): ${weatherData.sunshineHours}"
                            } else {
                                "Ingen Frost-stasjon funnet i nærheten."
                            }
                        }
                        "weatherforecast" -> mcpClient.getForecast(rLat, rLon)
                        "get_weather_alerts" -> mcpClient.getAlerts() // Nytt
                        else -> "Ukjent verktøy."
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

                // <------ ENDRING: Sender med 'tools'-parameteren i oppfølgingskallet slik at den kan sjekke været PÅ NYTT
                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = "claude-sonnet-4-5",
                        system = systemPrompt,
                        messages = conversation,
                        tools = listOf(frostTool)
                    )
                )
            }

            response.content.firstOrNull { it.type == "text" }?.text ?: "AI-en ga et svar uten tekst."

        } catch (e: HttpException) {
            "AI-feil: ${e.code()} - ${e.response()?.errorBody()?.string()}"
        } catch (e: Exception) {
            "Feil: ${e.message}"
        }
    }

    // <------ ENDRING: Lagt til funksjonen for FREMTIDIG råd
    suspend fun getFuturePlantRecommendations(location: SavedLocation): String {
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

        val systemPrompt = """
            Du er en profesjonell og velformulert hageekspert i Norge som skal gi råd til en hobbygartner/bruker.
            Du får fremtidig værinformasjon fra en API som heter Met Weather, du får følgende koordinater Lat: $lat, Lon: $lon.
            Du får informasjon om relevante planter på koordinatene.
            Du SKAL bruke verktøyene dine for å sjekke fremtidig vær som du får fra Weather API tool'en før du gir råd.
            Skriv også opp værinformasjonen du får for de kommende dagene slik at man får en oversikt.
            Baser rådene dine på disse plantene:
            $plantsText
            Svar konkret, strukturert og på Norsk.
        """.trimIndent()

        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre basert på informasjonen, plantene, været? Vis været og anbefalinger.")))
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
                            // <------ ENDRING: Bruker MetWeatherRepository her for å bygge en tekststreng for været
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val forecast = weatherRepo.getParsedForecast(rLat, rLon)
                            "Værvarsel: ${forecast?.temperature}°C, ${forecast?.weatherSymbol}, Vind: ${forecast?.windSpeed} m/s, Nedbør: ${forecast?.precipitation} mm."
                        }
                        else -> "Ukjent verktøy."
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
                        messages = conversation,
                        tools = listOf(weatherTool)
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