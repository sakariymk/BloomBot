package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import project.team36.data.klima.frost.getNearestStation
import project.team36.data.klima.frost.getTemperatures
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
}