package project.team36.ui.findplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.model.plant.PlantInfo
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedPlant
import project.team36.ui.map.SavedPlacesViewModel

class FindPlantsViewModel (
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val plants: StateFlow<List<PlantInfo>> = _plants.asStateFlow()

    init {
        _plants.value = plantRepository.initiatePlants()
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
    fun onPlantAdded(plant: PlantInfo) {
        _plants.value -= plant
    }
}