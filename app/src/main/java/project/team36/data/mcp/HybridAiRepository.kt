package project.team36.data.ai

import kotlinx.serialization.json.*
import project.team36.data.klima.MetWeatherApi
import project.team36.data.klima.frost.getNearestStation
import project.team36.data.klima.frost.getTemperatures
import project.team36.data.plante.PlantDataSource
import project.team36.model.ai.*
import retrofit2.HttpException

/**
 * Formål og funksjon:
 * Jeg har bygget denne klassen for å fungere som den sentrale "orkestratoren" i appen.
 * Det er her all magien skjer: Jeg henter lokale plantedata, snakker med Claude, og gir
 * AI-en evnen til å dynamisk slå opp sanntids vær- og frostdata før den svarer brukeren.
 *
 * Relevante filer:
 * - ClaudeApi / MetWeatherApi: Nettverkskontraktene jeg bruker her inne.
 * - ClaudeModels: Datastrukturen jeg fyller for å snakke med AI-en.
 * - PlantDataSource: Den lokale databasen hvor jeg henter plantelisten vår.
 */
class HybridAiRepository(
    // Jeg injiserer alle avhengighetene via konstruktøren. Dette gjør koden modulær og enkel å teste.
    private val claudeApi: ClaudeApi,
    private val metWeatherApi: MetWeatherApi,
    private val plantDataSource: PlantDataSource,
    private val frostClientId: String,
    private val claudeApiKey: String
) {

    // 1. Verktøydefinisjoner:
    // Her definerer jeg "bruksanvisningen" for frost-sjekken. Jeg forteller Claude
    // nøyaktig hva verktøyet gjør, og at han MÅ gi meg latitude og longitude for å bruke det.
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

    // Det samme gjør jeg for værmeldingen fra MET. Disse to verktøyene utstyrer AI-en min
    // med muligheten til å handle basert på sanntidsdata, ikke bare statisk kunnskap.
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

    // Dette er hovedfunksjonen som UI-et (MapViewModel) kaller når brukeren trykker på knappen.
    suspend fun getPlantRecommendations(lat: Double, lon: Double): String {

        // 2. Data-innsamling:
        // Først henter jeg plantene fra vår egen database (CSV-filen).
        val plants = try { plantDataSource.fetchPlants() } catch (e: Exception) { emptyList() }

        // Jeg formaterer en liste med de 15 første plantene til en tekststreng,
        // slik at jeg kan bake det inn i instruksene til AI-en.
        val plantsText = plants.take(15).joinToString("\n") {
            "- ${it.name}: Sone ${it.minZone}-${it.maxZone}, Frostsikker: ${it.isFrostResistant}"
        }

        // 3. System Prompt:
        // Dette er "hjernvasken" jeg gir Claude før han får svare. Jeg forteller ham rollen hans,
        // gir ham koordinatene, og gir en streng instruks om at han MÅ bruke verktøyene.
        val systemPrompt = """
            Du er en hageekspert i Norge. Brukeren er på lokasjonen Lat: $lat, Lon: $lon.
            Du SKAL bruke verktøyene dine for å sjekke vær og frost før du gir råd.
            Baser rådene dine på disse plantene:
            $plantsText
            Svar kort og vennlig på norsk.
        """.trimIndent()

        // 4. Samtaleloggen:
        // Jeg starter selve samtalen med brukerens spørsmål.
        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen nå?")))
        )

        return try {
            // FØRSTE API-KALL: Jeg sender prompten, spørsmålet, og verktøyene til Claude 4.6.
            var response = claudeApi.sendMessage(
                apiKey = claudeApiKey,
                request = ClaudeRequest(
                    model = "claude-sonnet-4-6", // Jeg bruker den nyeste og kraftigste modellen her.
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(frostTool, weatherTool)
                )
            )

            // 5. Verktøy-håndtering (Tool Use):
            // Jeg sjekker om Claude stoppet fordi han ønsker å bruke et av verktøyene jeg ga ham.
            if (response.stop_reason == "tool_use") {

                // Jeg legger Claudes forespørsel inn i samtaleloggen slik at han husker hva han spurte om.
                conversation.add(ClaudeMessage("assistant", response.content))

                // Jeg filtrerer ut alle verktøyene han vil bruke (i tilfelle han vil bruke både vær og frost samtidig).
                val toolUseBlocks = response.content.filter { it.type == "tool_use" }
                val toolResultBlocks = mutableListOf<ContentBlock>()

                // Jeg går gjennom hvert verktøy han ba om, ett for ett.
                for (block in toolUseBlocks) {
                    val toolId = block.id ?: continue
                    val toolName = block.name ?: ""

                    // Jeg henter ut koordinatene Claude ga meg, men bruker appens koordinater som fallback.
                    val inputJson = block.input?.jsonObject
                    val rLat = inputJson?.get("latitude")?.jsonPrimitive?.double ?: lat
                    val rLon = inputJson?.get("longitude")?.jsonPrimitive?.double ?: lon

                    // Jeg kjører faktisk koden for å hente været eller frostdata, avhengig av hva Claude ba om.
                    val resultText = when (toolName) {
                        "get_current_frost_status" -> {
                            val station = getNearestStation(rLat, rLon, frostClientId)
                            if (station != null) "Temp på ${station.name}: ${getTemperatures(station.id, frostClientId)}°C" else "Ingen stasjon funnet."
                        }
                        "weatherforecast" -> {
                            val metRes = metWeatherApi.getWeather(rLat, rLon)
                            if (metRes.isSuccessful) metRes.body()?.string()?.take(800) ?: "Tomt svar" else "Feil fra MET."
                        }
                        else -> "Ukjent verktøy."
                    }

                    // Jeg bygger resultatet inn i en blokk som er linket til Claudes opprinnelige forespørsel (tool_use_id).
                    toolResultBlocks.add(
                        ContentBlock(
                            type = "tool_result",
                            tool_use_id = toolId,
                            content = resultText
                        )
                    )
                }

                // Jeg sender alle resultatene fra verktøyene tilbake til AI-en fra brukerens side.
                conversation.add(ClaudeMessage("user", toolResultBlocks))

                // ANDRE API-KALL: Nå som Claude har fått vær- og frostdataene han ba om,
                // sender jeg samtalen på nytt slik at han kan skrive den endelige anbefalingen.
                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = "claude-sonnet-4-6",
                        system = systemPrompt,
                        messages = conversation
                    )
                )
            }

            // 6. Retur av data:
            // Jeg trekker ut den ferdige teksten fra Claudes siste svar og sender det tilbake til brukergrensesnittet.
            response.content.firstOrNull { it.type == "text" }?.text ?: "AI-en ga et svar uten tekst."

            // 7. Feilhåndtering:
            // Fanger opp eventuelle krasj (som manglende internett eller feil i JSON) slik at appen ikke lukker seg,
            // men heller viser en trygg feilmelding til brukeren.
        } catch (e: HttpException) {
            "AI-feil: ${e.code()} - ${e.response()?.errorBody()?.string()}"
        } catch (e: Exception) {
            "Feil: ${e.message}"
        }
    }
}