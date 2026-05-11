package project.team36.data.ai

import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.first
import project.team36.data.mcp.MetMcpClient
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.ai.*
import kotlinx.serialization.json.Json
import android.util.Log
import project.team36.model.location.SavedPlant


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
private data class CachedEntry(
    val response: AiRecommendationResponse,
    val timestamp: Long
)
// Repository som initialiserer samtalen med Claude og forsyner den med verktøy for vær og planter
class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    private val plantDataSource: LocationRepository,
    private val claudeApiKey: String
) {

    companion object {
        private const val MODEL_NAME = "claude-sonnet-4-5"
        private const val STOP_REASON_TOOL_USE = "tool_use"
        private const val MAX_TOOL_LOOPS = 3
        private const val LOG_TAG_AI_ERROR = "AI_ERROR"
        private const val TOOL_NAME_WEATHER_FORECAST = "weatherforecast"
        private const val TOOL_NAME_WEATHER_ALERTS = "get_weather_alerts"

        private val MARKDOWN_JSON_REGEX = Regex("```json|```")

        private const val CACHE_EXPIRATION_MS = 12 * 60 * 60 * 1000L
    }

    private val dailyCache = mutableMapOf<Long, CachedEntry>()

    // Definerer væroverktøyet slik at Claude vet hvilke parametere (lat/lon) som kreves for å bruke det
    private val weatherTool = ClaudeTool(
        name = TOOL_NAME_WEATHER_FORECAST,
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
        name = TOOL_NAME_WEATHER_ALERTS,
        description = "Henter aktive ekstremværvarsler fra Meteorologisk institutt for Norge. Bruk dette for å advare brukeren om farlige værforhold.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        )
    )

    // Fleksibel JSON-parser som forhindrer krasj dersom Claude returnerer uventede felt
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }


        // Henter brukerens lagrede planter for denne spesifikke hagen fra den lokale databasen
        suspend fun getPlantData (location: SavedLocation, plantDataSource: LocationRepository): List<SavedPlant> {
            val plants = try {
                plantDataSource.getPlantsForLocation(location.id).first()
            } catch (e: Exception) {
                emptyList()
            }
            return plants
        }

        // Formaterer plantelisten til en lesbar streng for Claude, eller gir en standardmelding hvis listen er tom
        suspend fun getPlantTxt(location: SavedLocation, plantDataSource: LocationRepository): String {
            val plantsText = if (getPlantData(location, plantDataSource).isEmpty()) {
                "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
            } else {
                getPlantData(location, plantDataSource).take(15).joinToString("\n") {
                    "- ${it.name}: Sone: ${it.climateZone}, Lysbehov: ${it.light}, Vann Per Uke: ${it.waterPerWeek}"
                }
            }
            return plantsText
        }

    private fun isCacheValid(entry: CachedEntry?): Boolean {
        if (entry == null) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - entry.timestamp) < CACHE_EXPIRATION_MS
    }

    // Henter inn vær og planter for lokasjonen, og ber Claude generere et strukturert JSON-svar med dagens råd
    suspend fun getPlantRecommendations(location: SavedLocation, forceRefresh: Boolean = false): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon     = location.lon ?: 0.0
        val plantsText = getPlantTxt(location, plantDataSource)
        val currentDate = java.time.LocalDate.now().toString()
        val locId = location.id
        val cached = dailyCache[locId]

        if (!forceRefresh && isCacheValid(cached)) {
            Log.d("AI_CACHE", "Returnerer lagret dagsråd for lokasjon: $locId")
            return cached?.response
        }

        // Setter opp systemprompten som gir Claude rollen sin og strenge instruksjoner for JSON-strukturen vi forventer
        val systemPrompt = """
            Du er en profesjonell, presis og velformulert hageekspert i Norge. Din oppgave er å gi skreddersydde, trygge og praktiske hageråd til en hobbygartner.

            KONTEKST FOR DAGEN:
            Dagens dato er: $currentDate. 
            Lokasjon (koordinater): Lat $lat, Lon $lon.

            Du har tilgang til verktøyet MetWeather. DU MÅ bruke dette verktøyet for å hente dagens faktiske vær på denne lokasjonen.

            Brukerens registrerte planter på denne lokasjonen:
            $plantsText

            ABSOLUTTE REGLER FOR RÅDGIVNING (Følg disse for å unngå systemfeil):
    
            1. TIDSFORSTÅELSE OG SESONG: Det er kritisk at du forstår hvilken måned og årstid vi er i basert på datoen ($currentDate). Du skal ALDRI foreslå oppgaver som er ulogiske for sesongen.

            2. FAKTABASERT VÆR: Du skal ALDRI gjette eller hallusinere været. Du må utelukkende basere rådene dine på værdataene du henter fra verktøyet ditt, og planteinformasjon du får fra plantene lagret på lokasjonen.

            3. HÅNDTERING AV PLANTER (Kritisk logikk for respons): 
               - HVIS LISTEN MED PLANTER IKKE ER TOM FOR GJELDENE LOKASJONEN: Du SKAL inkludere nøyaktig ett unikt og praktisk råd for HVER ENESTE plante i listen.
               - HVIS LISTEN MED PLANTER ER TOM FOR GJELDENE LOKASJON: Du må foreslå 2-3 anbefalte planter. I 'Recommend' under 'PlantAdvice' må det inneholde nøyaktig denne setningen: "Siden du enda ikke har valgt noen planter for denne lokasjonen, er dette noen arter vi sterkt anbefaler å vurdere nå."
               
            4. GENERELLE OPPGAVER: 'generalTasks' skal inneholde 2-3 overordnede, sesongriktige hageoppgaver som ikke er knyttet til én spesifikk plante.

            STRENGT JSON-FORMAT (KRITISK SYSTEMKRAV):
            Systemet som leser svaret ditt forventer RÅ JSON. Nøklene i objektet må ALDRI endres. Hvis du bryter disse reglene, krasjer appen:
            - Du MÅ returnere svaret ditt UTELUKKENDE som et gyldig JSON-objekt.
            - INGEN introduksjonstekst før JSON-objektet.
            - INGEN markdown-formatering (IKKE bruk ```json).
            - Unngå bruk av uescapede anførselstegn inne i selve tekstverdiene.

            Svaret ditt skal starte med '{' og slutte med '}', og matche denne strukturen nøyaktig hver gang:
            {
              "plantAdvice": [
                { 
                  "Recommend" : "Setning hvis det IKKE er planter på lokasjonen",
                  "plantName": "Navn på eksisterende eller anbefalt plante", 
                  "advice": "Spesifikt og handling-orientert råd for i dag." 
                }
              ],
              "generalTasks": [
                "Generell oppgave 1", 
                "Generell oppgave 2"
              ]
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
                    model = MODEL_NAME,
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(weatherTool, alertTool)
                )
            )

            var toolLoops = 0
            // Løkke som lar Claude bruke verktøy. Vi setter en maksgrense på 3 iterasjoner for å unngå uendelige looper
            while (response.stop_reason == STOP_REASON_TOOL_USE && toolLoops < MAX_TOOL_LOOPS) {
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
                        TOOL_NAME_WEATHER_FORECAST -> {
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val forecast = weatherRepo.getParsedForecast(rLat, rLon)
                            "Værvarsel: ${forecast?.temperature}°C, ${forecast?.weatherSymbol}, Vind: ${forecast?.windSpeed} m/s, Nedbør: ${forecast?.precipitation} mm."
                        }
                        TOOL_NAME_WEATHER_ALERTS -> {
                            val weatherRepo = project.team36.data.mcp.MetWeatherRepository(mcpClient)
                            val alerts = weatherRepo.getAllAlerts()
                            if (alerts.isEmpty()) "Ingen aktive ekstremværvarsler."
                            else alerts.joinToString("\n") { "${it.event}: ${it.description}" }
                        }
                        else -> {
                            Log.w(LOG_TAG_AI_ERROR, "Ukjent verktøy: $toolName")
                            "Ukjent verktøy."
                        }
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
                        model = MODEL_NAME,
                        system = systemPrompt,
                        messages = conversation,
                        tools = listOf(weatherTool, alertTool)
                    )
                )
            }

            // Finner selve tekstblokken i det endelige svaret fra Claude
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null

            // Fjerner eventuelle markdown-tagger (som ```json) Claude noen ganger legger til
            val cleanJson = rawText.replace(MARKDOWN_JSON_REGEX, "").trim()
            // Konverterer den rene JSON-strengen over til dataklassen vår
            val parsedData = jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)
            Log.d("AI_RAW", "Dette svarte Claude før parsing:\n$cleanJson")

            dailyCache[locId] = CachedEntry(
                response = parsedData,
                timestamp = System.currentTimeMillis()
            )
            return parsedData

        } catch (e: Exception) {
            Log.e(LOG_TAG_AI_ERROR, "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }

}
