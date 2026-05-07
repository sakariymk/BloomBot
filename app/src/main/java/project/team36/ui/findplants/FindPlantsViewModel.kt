package project.team36.ui.findplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.model.plant.PlantInfo
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.PlantDetails
import project.team36.ui.plant.toPlantDetails

class FindPlantsViewModel (
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantDetails>>(emptyList())
    val plants: StateFlow<List<PlantDetails>> = _plants.asStateFlow()
    private val _notRecommendedPlants = MutableStateFlow<List<PlantDetails>>(emptyList())
    val notRecommendedPlants: StateFlow<List<PlantDetails>> = _notRecommendedPlants.asStateFlow()

    private val _pendingPlant = MutableStateFlow<PlantDetails?>(null)
    val pendingPlant: StateFlow<PlantDetails?> = _pendingPlant.asStateFlow()

    init {
        _plants.value = plantRepository.initiatePlants().map { it.toPlantDetails() }
    }

    fun addPlant(plant: PlantInfo) {
        viewModelScope.launch {
            val locationId = savedPlacesViewModel.selectedLocation.value?.id ?: return@launch
            locationRepository.insertPlant(
                SavedPlant(
                    locationId = locationId,
                    name = plant.name,
                    soil = plant.soil,
                    light = plant.light,
                    waterPerWeek = plant.waterPerWeek,
                    plantingMonth = plant.plantingMonth,
                    climateZone = plant.climateZone,
                    hasBeenWatered = plant.hasBeenWatered,
                    daysSinceWatered = plant.daysSinceWatered,
                    maxDaysWithoutWater = plant.maxDaysWithoutWater,
                    imageRes = plant.imageRes,
                    description = plant.description
                )
            )
        }
    }

    fun onPlantAdded(plant: PlantDetails) {
        _plants.value -= plant
    }

    fun requestAddPlant(plant: PlantDetails) {
        _pendingPlant.value = plant
    }

    fun dismissAddPlant() {
        _pendingPlant.value = null
    }

    fun confirmAddPlant(plant: PlantDetails, location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.insertPlant(
                SavedPlant(
                    locationId = location.id,
                    name = plant.name,
                    soil = plant.soil,
                    light = plant.light,
                    waterPerWeek = plant.waterPerWeek,
                    plantingMonth = plant.plantingMonth,
                    climateZone = plant.climateZone,
                    hasBeenWatered = false,
                    daysSinceWatered = 0,
                    maxDaysWithoutWater = plant.maxDaysWithoutWater,
                    imageRes = plant.imageRes,
                    description = plant.description,
                    lastWateredDate = 0L
                )
            )
            _pendingPlant.value = null
        }
    }

    fun loadRecommendedPlants(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.getPlantsForLocation(location.id)
                .first()
                .let { savedPlants ->
                    val recommended = plantRepository.recommendPlants(location, savedPlants)
                        .map { it.toPlantDetails() }
                    _plants.value = recommended

                    val notRecommended = plantRepository.notRecommendedPlants(location, savedPlants)
                        .map { it.toPlantDetails() }
                    _notRecommendedPlants.value = notRecommended
                }
        }
    }
}


class FindPlantsViewModelFactory(
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FindPlantsViewModel(plantRepository, locationRepository, savedPlacesViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}