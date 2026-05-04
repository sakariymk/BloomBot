package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import project.team36.data.ai.ClaudeApi
import project.team36.model.ai.ClaudeMessage
import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ContentBlock
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Formål og funksjon:
 * Dette er vår integrasjonstest for AI-funksjonaliteten. Formålet her er utelukkende å sjekke
 * at vi klarer å nå Anthropics servere, at API-nøkkelen er gyldig, og at dataklassene
 * våre (ClaudeRequest og ClaudeResponse) blir konvertert riktig til og fra JSON.
 */
class AiIntegrationTest {

    // Husk å bytte ut denne strengen med den faktiske API-nøkkelen deres før du kjører testen!
    private val testApiKey = "***FJERNET***"

    // Vi bygger opp Retrofit-klienten manuelt her i testen, akkurat slik den settes opp i MainActivity.
    // Vi bruker 'by lazy' slik at den bare bygges dersom testen faktisk kjøres.
    private val claudeApi: ClaudeApi by lazy {
        val contentType = "application/json".toMediaType()

        // Samme JSON-regler som i appen: ignorer ukjente felt, og tillat standardverdier
        val jsonConfig = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()
            .create(ClaudeApi::class.java)
    }

    @Test
    fun testClaudeApiConnection(): Unit = runBlocking {
        println("Starter test av Claude API-tilkobling...")

        // 1. Vi bygger en veldig enkel forespørsel uten system-prompt eller verktøy.
        // Vi ber bare Claude om å bekrefte at han hører oss.
        val testRequest = ClaudeRequest(
            model = "claude-sonnet-4-5", // Bruk den spesifikke versjonen dere har tilgang til
            max_tokens = 100,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = listOf(ContentBlock(type = "text", text = "Hei Claude! Svar kun med ordet 'Suksess' hvis systemet fungerer."))
                )
            )
        )

        try {
            // 2. Vi sender forespørselen opp til serveren.
            val response = claudeApi.sendMessage(testApiKey, testRequest)

            // 3. Vi kjører strenge sjekker (assertions) for å forsikre oss om at vi ikke fikk null tilbake
            assertNotNull("API-kallet returnerte null. Sjekk internett og API-nøkkel.", response)
            assertTrue("Svaret fra Claude mangler innhold.", response.content.isNotEmpty())

            // 4. Vi graver ut den faktiske teksten fra svaret, akkurat slik HybridAiRepository gjør det
            val svarTekst = response.content.firstOrNull { it.type == "text" }?.text

            // 5. Printer resultatet i konsollen slik at vi kan lese det
            println("--- Test results ---")
            println("Fikk svar fra Claude: $svarTekst")
            println("Stop reason: ${response.stop_reason}")
            println("---------------------")

            // Sjekker at svaret faktisk inneholder teksten vi forventet
            assertNotNull("Klarte ikke å parse tekst fra svaret.", svarTekst)

        } catch (e: Exception) {
            // Hvis noe tryner (f.eks. 401 Unauthorized pga feil nøkkel), fanger vi det og printer feilen
            println("Test feilet! Feilmelding: ${e.message}")
            throw e
        }
    }
}