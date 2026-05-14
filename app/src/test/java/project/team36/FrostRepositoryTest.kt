package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import project.team36.data.klima.frost.getNearestStation
import project.team36.data.klima.frost.getPrecipitation
import project.team36.data.klima.frost.getTemperatures
import project.team36.data.klima.frost.getDurationSunShine

import project.team36.model.frost.Source

class FrostApiTest {

    @Test
    fun testGetNearestStation(): Unit = runBlocking {
        val testLat = 59.9423
        val testLon = 10.7213

        val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

        val station: Source? = getNearestStation(testLat, testLon, clientId)

        assertNotNull("API-call returned null. Check the client ID.", station)

        station?.let {
            println("--- Test results ---")
            println("Found station: ${it.name}")
            println("ID: ${it.id}")
            println("Municipality: ${it.municipality}")
            println("Coordinates: ${it.geometry.coordinates}")
            println("---------------------")

            assertNotNull(it.id)

        }
    }

    @Test
    fun testGetTemperatures() : Unit = runBlocking{
        val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

        val testStationId = "SN18700"

        val temperature : Double? = getTemperatures(testStationId,clientId)
        assertNotNull("API-call returned null. Check the client ID.", temperature)

        temperature?.let {
            println("--- Test results ---")
            println("Temprature on station $testStationId: $it C")
            println("---------------------")
        }

    }
    val testStations = listOf(
        "SN18700", "SN50500", "SN76920", "SN90450", "SN17850", "SN24120", "SN39040", "SN47300", "SN52310", "SN68010",
        "SN76956", "SN93700", "SN18210", "SN27500", "SN82290", "SN100", "SN400", "SN470", "SN700", "SN800",
        "SN1150", "SN1320", "SN1450", "SN1650", "SN1950", "SN2150", "SN2350", "SN2550", "SN2700", "SN2900"
    )

    @Test
    fun testGetPrecipitation() : Unit = runBlocking {
        val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

        val testDate = "2025-03-09"

        println("--------Test results-------------")

        testStations.forEach { testStationId ->
            val percipitation : Double? = getPrecipitation(testStationId, clientId, testDate)

            if (percipitation == null) {
                println("Skipping station $testStationId: No data found for this date.")
            }
        }

        println("---------------------")
    }

    @Test
    fun getDurationSunShine() : Unit = runBlocking {
        val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"



        val testDate = "2025-03-09"

        println("--------Test results-------------")

        testStations.forEach { testStationId ->
            val sunshine : Double? = getDurationSunShine(testStationId, clientId, testDate)

            if (sunshine == null) {
                println("No available data")
            }
        }

        println("---------------------")
    }
}
