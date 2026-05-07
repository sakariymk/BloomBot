package project.team36.data.plant

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantRepositoryTest {

    private val mockDataSource = mockk<PlantDataSource>(relaxed = true)
    private val mockLocationRepo = mockk<LocationRepository>(relaxed = true)
    private lateinit var plantRepository: PlantRepository

    private fun testPlant(name: String, climateZone: Int) = PlantInfo(
        name = name,
        climateZone = climateZone,
        soil = setOf(SoilType.SOIL),
        light = setOf(LightType.FULL),
        isFrostResistant = false,
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
        isFrostResistant = false,
        waterPerWeek = 1.0f,
        plantingMonth = setOf(5),
        climateZone = 3,
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
            testPlant("Rose", climateZone = 3),
            testPlant("Tulipan", climateZone = 2),
            testPlant("Palme", climateZone = 5)
        )
        every { mockDataSource.fetchPlants() } returns plants
        plantRepository = PlantRepository(mockDataSource, mockLocationRepo)
    }


    @Test
    fun initiatePlants_returnsAllPlants() {
        val result = plantRepository.initiatePlants()
        assertEquals(3, result.size)
    }

    @Test
    fun testPlantsSuitableForZone() {
        val result = plantRepository.recommendPlants(testLocation(zone = 3), emptyList())

        assertTrue("Rose (zone 3) should be recommended for zone 3", result.any { it.name == "Rose" })
        assertFalse("Palme (zone 5) should not be recommended for zone 3", result.any { it.name == "Palme" })
    }

    @Test
    fun testAlreadySavedPlants() {
        val savedPlants = listOf(testSavedPlant("Rose"))
        val result = plantRepository.recommendPlants(testLocation(zone = 3), savedPlants)

        assertFalse("Rose is already saved and should be excluded", result.any { it.name == "Rose" })
    }

    @Test
    fun testPlantsTooWarmForZone() {
        val result = plantRepository.notRecommendedPlants(testLocation(zone = 3), emptyList())

        assertTrue("Palme (zone 5) should not be recommended for zone 3", result.any { it.name == "Palme" })
        assertFalse("Rose (zone 3) should not appear as not-recommended", result.any { it.name == "Rose" })
    }

    @Test
    fun testexcludesAlreadySavedPlantsNotRecommend() {
        val savedPlants = listOf(testSavedPlant("Tulipan"))

        val result = plantRepository.notRecommendedPlants(testLocation(zone = 1), savedPlants)

        assertFalse("Tulipan is already saved and should be excluded", result.any { it.name == "Tulipan" })
    }
}