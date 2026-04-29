package project.team36.data.plant

import kotlinx.coroutines.flow.first
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo

class PlantRepository(
    private val plantDataSource: PlantDataSource,
    private val locationRepository: LocationRepository
) {
    private var cachedPlants: List<PlantInfo> = emptyList()

    fun initiatePlants(): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        return cachedPlants
    }

    // Anbefaler planter basert på lokasjon

    fun recommendPlants(location: SavedLocation, savedPlants: List<SavedPlant>): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }

        val savedPlantNames = savedPlants.map { it.name }.toSet()

        return cachedPlants.filter {
            it.climateZone >= location.zone && it.name !in savedPlantNames
        }
    }

    fun notRecommendedPlants(location: SavedLocation, savedPlants: List<SavedPlant>): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        val savedPlantNames = savedPlants.map { it.name }.toSet()

        return cachedPlants.filter {
            it.climateZone < location.zone && it.name !in savedPlantNames
        }
    }

    // Returnerer alle lagrede steder en spesifik plante kan plantes

    suspend fun locationsForPlants(plantName: String): List<String> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        val plant = cachedPlants.find { it.name == plantName }
            ?: return emptyList()

        val allLocations = locationRepository.allLocations.first()

        return allLocations
            .filter { plant.climateZone >= it.zone }
            .mapNotNull { it.name }
    }
}