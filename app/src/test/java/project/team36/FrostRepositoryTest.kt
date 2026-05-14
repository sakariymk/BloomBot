package project.team36

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository
import project.team36.model.frost.WeatherData

class FrostRepositoryTest  {

    // Lager mock av repository
    private val mockFrostRepo = mockk<FrostRepository> (relaxed = true)

    // Hjelpefunksjon for å lage test av WeatherData
    private fun testWeatherData() = WeatherData (
        stationId = "SN18700",
        temperature = 15.0,
        precipitation = 1.2,
        sunshineHours = 4.5

    )

    //Funksjonen gjør at repository returnerer gyldig værdata
    @Test
    fun testGetFullWeatherForLocationSuccess() : Unit = runBlocking {
        val expectedWeather = testWeatherData()

        every {runBlocking {mockFrostRepo.getFullWeatherForLocation(
            any(),
            any(),
            any())}} returns expectedWeather

        val result = mockFrostRepo.getFullWeatherForLocation(59.9,10.7, "latest")

        //Sjekker at dataene stemmer
        assertNotNull("Værdata skal ikke være null", result)
        assertEquals("SN18700", result?.stationId)
        assertEquals(15.0, result?.temperature!!, 0.1)
        println("Suksess: Hentet temperatur ${result.temperature} fra stasjon ${result.stationId}")
    }

    @Test
    fun testPrecipitationSinceLastUsed() : Unit = runBlocking {
        // Lager et map med nebørsdata per dato
        val mockPrecipitationMap = mapOf(
            "2026-05-01" to 2.5,
            "2026-05-02" to 0.0,
            "2026-05-03" to 5.0
        )
        every {runBlocking {mockFrostRepo.getPrecipitationSinceLastUsed(
            any(),
            any(),
            any()
        )}} returns mockPrecipitationMap

        //Henter nedbørhistorikk
        val result = mockFrostRepo.getPrecipitationSinceLastUsed(59.9, 10.7,"2026-05-01")

        //Sjekker at mappen inneholder forventet data
        assertNotNull(result)
        assertEquals(3, result?.size)
        assertEquals(5.0, result?.get("2026-05-03")!!, 0.1)
        println("Suksess: Fant nedbør for ${result.size} dager")
    }
}