package project.team36.data.ai

import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.first
import project.team36.data.mcp.MetMcpClient
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.ai.*
import kotlinx.serialization.json.Json
import android.util.Log


/**
 * Formål og funksjon:
 * Dette er hjernen i AI-integrasjonen vår. Filen styrer hele dialogen mellom appen og Claude.
 * Den henter ut brukerens lagrede planter, bygger strenge prompts for å tvinge AI-en til å svare i JSON,
 * og gir Claude tilgang til verktøy for å sjekke været. Til slutt oversetter den Claudes
 * tekstsvar til strukturerte Kotlin-objekter som UI-et kan bruke.
 *
 * Relevante filer:
 * - ClaudeApi: Retrofit-grensesnittet som sender selve HTTP-forespørslene.
 * - AiRecommendationModels: Dataklassene (AiRecommendationResponse) som vi parser JSON-svaret over til.
 * - MetWeatherRepository / MetMcpClient: Verktøyene Claude bruker for å hente faktisk værdata.
 * - LocationRepository: Databasen hvor vi henter brukerens planter fra.
 */

// Repository som initialiserer samtalen med Claude og forsyner den med verktøy for vær og planter
class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    private val plantDataSource: LocationRepository,
    private val claudeApiKey: String
) {

    // Definerer væroverktøyet slik at Claude vet hvilke parametere (lat/lon) som kreves for å bruke det
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

    // Definerer verktøy for ekstremvær. Dette krever ingen input-parametere fra Claude
    private val alertTool = ClaudeTool(
        name = "get_weather_alerts",
        description = "Henter aktive ekstremværvarsler fra Meteorologisk institutt for Norge. Bruk dette for å advare brukeren om farlige værforhold.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        )
    )

    // Fleksibel JSON-parser som forhindrer krasj dersom Claude returnerer uventede felt
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // Henter inn vær og planter for lokasjonen, og ber Claude generere et strukturert JSON-svar med dagens råd
    suspend fun getPlantRecommendations(location: SavedLocation): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0

        // Henter brukerens lagrede planter for denne spesifikke hagen fra den lokale databasen
        val plants = try { plantDataSource.getPlantsForLocation(location.id).first() } catch (e: Exception) { emptyList() }

        // Formaterer plantelisten til en lesbar streng for Claude, eller gir en standardmelding hvis listen er tom
        val plantsText = if (plants.isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            plants.take(15).joinToString("\n") {
                "- ${it.name}: Sone: ${it.climateZone}, Lysbehov: ${it.light}, Vann Per Uke: ${it.waterPerWeek}, Frostsikker: ${it.isFrostResistant}"
            }
        }

        // Setter opp systemprompten som gir Claude rollen sin og strenge instruksjoner for JSON-strukturen vi forventer
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

        // Starter samtaleloggen med brukerens initiell prompt
        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen nå? Svar KUN med JSON.")))
        )

        return try {
            // Sender den første forespørselen til Claude med tilgang til verktøyene våre
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
            // Løkke som lar Claude bruke verktøy. Vi setter en maksgrense på 3 iterasjoner for å unngå uendelige looper
            while (response.stop_reason == "tool_use" && toolLoops < 3) {
                toolLoops++

                // Legger Claudes verktøyforespørsel til i loggen slik at den husker konteksten
                conversation.add(ClaudeMessage("assistant", response.content))

                val toolUseBlocks = response.content.filter { it.type == "tool_use" }
                val toolResultBlocks = mutableListOf<ContentBlock>()

                // Går gjennom hvert verktøy Claude ba om å kjøre i denne runden
                for (block in toolUseBlocks) {
                    val toolId = block.id ?: continue
                    val toolName = block.name ?: ""

                    // Henter ut koordinatene Claude sender med. Hvis de mangler, bruker vi hagens koordinater
                    val inputJson = block.input?.jsonObject
                    val rLat = inputJson?.get("latitude")?.jsonPrimitive?.double ?: lat
                    val rLon = inputJson?.get("longitude")?.jsonPrimitive?.double ?: lon

                    // Kjører funksjonen som korresponderer med verktøyet Claude ba om
                    val resultText = when (toolName) {
                        "weatherforecast" -> {
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val forecast = weatherRepo.getParsedForecast(rLat, rLon)
                            "Værvarsel: ${forecast?.temperature}°C, ${forecast?.weatherSymbol}, Vind: ${forecast?.windSpeed} m/s, Nedbør: ${forecast?.precipitation} mm."
                        }
                        else -> "Ukjent verktøy."
                    }

                    // Pakker resultatet inn i et format Claude forstår, og legger det i listen
                    toolResultBlocks.add(
                        ContentBlock(type = "tool_result", tool_use_id = toolId, content = resultText)
                    )
                }

                // Legger alle verktøyresultatene inn i samtalen som en ny melding fra brukeren
                conversation.add(ClaudeMessage("user", toolResultBlocks))

                // Sender samtalen tilbake til Claude med de nye værdataene, slik at den kan generere endelig svar
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

            // Finner selve tekstblokken i det endelige svaret fra Claude
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null

            // Fjerner eventuelle markdown-tagger (som ```json) Claude noen ganger legger til
            val cleanJson = rawText.replace(Regex("```json|```"), "").trim()

            // Konverterer den rene JSON-strengen over til dataklassen vår
            jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)

        } catch (e: Exception) {
            Log.e("AI_ERROR", "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }

    // Identisk logikk som getPlantRecommendations, men med en prompt skreddersydd for fremtidsvarsler
    suspend fun getFuturePlantRecommendations(location: SavedLocation): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon = location.lon ?: 0.0

        // Henter planter fra database
        val plants = try { plantDataSource.getPlantsForLocation(location.id).first() } catch (e: Exception) { emptyList() }

        // Formaterer plantelisten
        val plantsText = if (plants.isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            plants.take(15).joinToString("\n") {
                "- ${it.name}: Sone ${it.climateZone}, Frostsikker: ${it.isFrostResistant}, VannperUke: ${it.waterPerWeek}"
            }
        }

        // Systemprompt spesifikt for fremtidsråd, krever også her streng JSON-retur
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
            // Første API-kall for fremtidsdata
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
            // Håndterer verktøybruk (hente fremtidsvær)
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

                    // Kjører vær-verktøyet
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

                // Sender værdata tilbake til Claude for endelig analyse
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

            // Henter tekstsvaret, renser bort markdown og parser til JSON-objekt
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null
            val cleanJson = rawText.replace(Regex("```json|```"), "").trim()

            jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)

        } catch (e: Exception) {
            Log.e("AI_ERROR", "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }
}