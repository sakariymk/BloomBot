package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import project.team36.data.ai.ClaudeApi
import project.team36.data.mcp.HybridAiRepository
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetMcpClient
import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ClaudeResponse
import project.team36.model.ai.ContentBlock
import project.team36.model.location.SavedLocation

/**
 * Formål og funksjon:
 * Dette er en enhetstest (Unit Test) for HybridAiRepository. Formålet her er å teste
 * logikken vår, spesielt at JSON-parsingen fungerer nøyaktig som den skal.
 * * Relevante filer:
 * - HybridAiRepository: Selve klassen vi tester funksjonaliteten til.
 * - AiRecommendationModels: Dataklassene vi sjekker at JSON-teksten oversettes til.
 */
class HybridAiRepositoryTest {

    @Test
    fun testGetPlantRecommendations_ParsesJsonCorrectly(): Unit = runBlocking {
        println("Starter test av JSON-parsing i HybridAiRepository...")

        val mockMcpClient = mock(MetMcpClient::class.java)
        val mockLocationRepo = mock(LocationRepository::class.java)

        val fakeClaudeApi = object : ClaudeApi {
            override suspend fun sendMessage(apiKey: String, request: ClaudeRequest): ClaudeResponse {
                val fakeJson = """
                    {
                      "plantAdvice": [
                        { "plantName": "Rose", "advice": "Husk å vanne rikelig i dag." }
                      ],
                      "generalTasks": ["Rak løv", "Sjekk for ugress"]
                    }
                """.trimIndent()

                return ClaudeResponse(
                    content = listOf(ContentBlock(type = "text", text = fakeJson)),
                    stop_reason = "end_turn"
                )
            }
        }

        // Vi initialiserer selve repositoryet vi vil teste, og mater inn de falske byggeklossene våre.
        val repository = HybridAiRepository(
            claudeApi = fakeClaudeApi,
            mcpClient = mockMcpClient,
            plantDataSource = mockLocationRepo,
            claudeApiKey = "test-key"
        )

        // Vi oppretter en fiktiv lokasjon som kreves som parameter for å kjøre funksjonen vi tester.
        val testLocation = SavedLocation(id = 1L, name = "Testhagen", lat = 59.9, lon = 10.7, address = "", zone = 1)

        // Vi kjører den faktiske logikken i HybridAiRepository for å se hva den returnerer.
        val response = repository.getPlantRecommendations(testLocation)

        // Vi sjekker at vi faktisk fikk tilbake et objekt (altså at JSON-parsingen IKKE krasjet og returnerte null).
        assertNotNull("JSON-parsingen feilet, returnerte null!", response)

        // Hvis vi kom så langt, printer vi ut verdiene for å bekrefte at dataene ble lagt på riktig plass.
        response?.let {
            println("--- Test results ---")
            println("Første planteråd: ${it.plantAdvice[0].plantName} - ${it.plantAdvice[0].advice}")
            println("---------------------")

            assertEquals(1, it.plantAdvice.size)
            assertEquals("Rose", it.plantAdvice[0].plantName)
            assertEquals("Husk å vanne rikelig i dag.", it.plantAdvice[0].advice)
            assertEquals(2, it.generalTasks.size)
            assertEquals("Rak løv", it.generalTasks[0])
            assertEquals("Sjekk for ugress", it.generalTasks[1])
        }
    }
    @Test
    fun testClaudeHallucination_InvalidJsonReturnsNull(): Unit = runBlocking {
        println("Starter test av AI-hallusinasjon (ugyldig JSON)...")

        val mockMcpClient = mock(MetMcpClient::class.java)
        val mockLocationRepo = mock(LocationRepository::class.java)

        val hallucinatingClaudeApi = object : ClaudeApi {
            override suspend fun sendMessage(apiKey: String, request: ClaudeRequest): ClaudeResponse {
                val badJson = "Hei! Det er en nydelig dag ute. Et godt tips er å vanne rosene dine i dag."

                return ClaudeResponse(
                    content = listOf(ContentBlock(type = "text", text = badJson)),
                    stop_reason = "end_turn"
                )
            }
        }

        // Vi initialiserer repositoryet med den "dårlige" AI-en
        val repository = HybridAiRepository(
            claudeApi = hallucinatingClaudeApi,
            mcpClient = mockMcpClient,
            plantDataSource = mockLocationRepo,
            claudeApiKey = "test-key"
        )

        // Fiktiv lokasjon for testen
        val testLocation = SavedLocation(id = 1L, name = "Krasjhagen", lat = 59.9, lon = 10.7, address = "", zone = 1)

        // Vi kaller funksjonen. Parseren (jsonParser) vil prøve å lese "Hei! Det er...",
        // innse at det ikke er gyldig JSON, kaste en exception, og try/catch-blokken
        // skal da fange feilen og returnere null.
        val response = repository.getPlantRecommendations(testLocation)

        println("--- Test results ---")
        println("Fikk svar fra repository: $response")
        println("---------------------")

        // Sjekker at svaret faktisk ble null, og at appen vår håndterte AI-feilen elegant
        assertNull("Repositoryet skal returnere null når AI-en svarer med ugyldig JSON format.", response)
    }
}