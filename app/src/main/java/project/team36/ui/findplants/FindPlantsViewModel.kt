package project.team36.ui.findplants

import android.util.Log
import androidx.lifecycle.ViewModel
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

    //TODO: få disse til å hente ikke-anbefalte planter
    private val _notRecommendedPlants = MutableStateFlow<List<PlantDetails>>(emptyList())
    val notRecommendedPlants: StateFlow<List<PlantDetails>> = _notRecommendedPlants.asStateFlow()

    private val _pendingPlant = MutableStateFlow<PlantDetails?>(null)
    val pendingPlant: StateFlow<PlantDetails?> = _pendingPlant.asStateFlow()

    init {
        _plants.value = plantRepository.initiatePlants().map { it.toPlantDetails() }
    }

    //Legg til plante i en location
    fun addPlant(plant: PlantInfo) {
        viewModelScope.launch {
            val locationId= savedPlacesViewModel.selectedLocation.value?.id ?: return@launch
            locationRepository.insertPlant(
                SavedPlant(
                    locationId = locationId,
                    name = plant.name,
                    soil = plant.soil,
                    light = plant.light,
                    isFrostResistant = plant.isFrostResistant,
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

    //Når plante legges til i en location
    fun onPlantAdded(plant: PlantDetails) {
        _plants.value -= plant
    }

    //når en bruker vil lagre plante -- bør ikke alt under gå på FindPlantsViewModel kanskje? for riktig sted for logikk
    fun requestAddPlant(plant: PlantDetails) {
        _pendingPlant.value = plant
    }

    fun dismissAddPlant() {
        _pendingPlant.value = null
    }

    //når bruker har valgt plante og lokasjon
    fun confirmAddPlant(plant: PlantDetails, location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.insertPlant(
                SavedPlant(
                    locationId = location.id,
                    name = plant.name,
                    soil = plant.soil,
                    light = plant.light,
                    isFrostResistant = plant.isFrostResistant,
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
                    Log.d("FindPlants", "Location zone: ${location.zone}, savedPlants: ${savedPlants.size}")
                    val result = plantRepository.recommendPlants(location, savedPlants)
                        .map { it.toPlantDetails() }
                    Log.d("FindPlants", "Recommended plants: ${result.size}")
                    _plants.value = result
                }
        }
    }

}