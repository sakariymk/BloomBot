package project.team36.data.plant

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantRepositoryTest {

    private val mockDataSource = mockk<PlantDataSource>(relaxed = true)
    private lateinit var plantRepository: PlantRepository

    private fun testPlant(name: String, climateZone: Int) = PlantInfo(
        name = name,
        climateZone = climateZone,
        soil = setOf(SoilType.SOIL),
        light = setOf(LightType.FULL),
        waterPerWeek = 1.0f,
        plantingMonth = setOf(5),
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 7,
        imageRes = 0,
        description = ""
    )

    private fun testSavedPlant(name: String, locationId: Long = 1L) = SavedPlant(
        locationId = locationId,
        name = name,
        soil = setOf(SoilType.SOIL),
        light = setOf(LightType.FULL),
        waterPerWeek = 1.0f,
        plantingMonth = setOf(5),
        climateZone = 4,
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 7,
        imageRes = 0,
        description = ""
    )

    private fun testLocation(zone: Int) = SavedLocation(
        name = "TestHage",
        lat = 60.0,
        lon = 10.0,
        address = "Gateveien",
        zone = zone
    )

    @Before
    fun setup() {
        val plants = listOf(
            testPlant("Rose", climateZone = 4),
            testPlant("Tulipan", climateZone = 6),
            testPlant("Palme", climateZone = 2)
        )
        every { mockDataSource.fetchPlants() } returns plants
        plantRepository = PlantRepository(mockDataSource)
    }

    @Test
    fun initiatePlants_returnsAllPlants() {
        val result = plantRepository.initiatePlants()
        assertEquals(3, result.size)
    }

    @Test
    fun testPlantsSuitableForZone() {
        val result = plantRepository.recommendPlants(testLocation(zone = 4), emptyList())

        assertTrue("Rose (climateZone 4) skal anbefales for sone 4", result.any { it.name == "Rose" })
        assertTrue("Tulipan (climateZone 6) skal anbefales for sone 4", result.any { it.name == "Tulipan" })
        assertFalse("Palme (climateZone 2) skal ikke anbefales for sone 4", result.any { it.name == "Palme" })
    }

    @Test
    fun testAlreadySavedPlants() {
        val savedPlants = listOf(testSavedPlant("Rose"))
        val result = plantRepository.recommendPlants(testLocation(zone = 4), savedPlants)

        assertFalse("Rose er allerede lagret og skal ekskluderes", result.any { it.name == "Rose" })
    }

    @Test
    fun testPlantsTooWarmForZone() {
        val result = plantRepository.notRecommendedPlants(testLocation(zone = 4), emptyList())

        assertTrue("Palme (climateZone 2) skal være i andre planter for sone 4", result.any { it.name == "Palme" })
        assertFalse("Rose (climateZone 4) skal ikke være i andre planter", result.any { it.name == "Rose" })
        assertFalse("Tulipan (climateZone 6) skal ikke være i andre planter", result.any { it.name == "Tulipan" })
    }

    @Test
    fun testExcludesAlreadySavedPlantsNotRecommend() {
        val savedPlants = listOf(testSavedPlant("Palme"))
        val result = plantRepository.notRecommendedPlants(testLocation(zone = 4), savedPlants)

        assertFalse("Palme er allerede lagret og skal ekskluderes", result.any { it.name == "Palme" })
    }
}