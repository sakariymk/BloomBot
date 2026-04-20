package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo
import project.team36.ui.map.SavedPlace
import project.team36.ui.map.SavedPlacesViewModel

class MyPlantsViewModel(
    private val repository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModel() {

    private val _pendingPlant = MutableStateFlow<PlantInfo?>(null)
    val pendingPlant: StateFlow<PlantInfo?> = _pendingPlant.asStateFlow()

    val plants = savedPlacesViewModel.selectedLocation
        .flatMapLatest { location ->
            repository.getPlantsForLocation(location?.id ?: -1)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Slette plante
    fun removePlant(plant: SavedPlant) {
        viewModelScope.launch {
            repository.deletePlant(plant)
        }
    }

    // for å slette plante basert på navn, brukes i AppNavHost på PlantDetailsScreen
    fun removePlantByName(plantName: String) {
        viewModelScope.launch {
            val plant = plants.value.find { it.name == plantName }
            plant?.let { repository.deletePlant(it) }
        }
    }

    //når en bruker vil lagre plante
    fun requestAddPlant(plant: PlantInfo) {
        _pendingPlant.value = plant
    }

    fun dismissAddPlant() {
        _pendingPlant.value = null
    }

    //når bruker har valgt plante og lokasjon
    fun confirmAddPlant(plant: PlantInfo, location: SavedLocation) {
        viewModelScope.launch {
            repository.insertPlant(
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
                    description = plant.description
                )
            )
            _pendingPlant.value = null
        }
    }

}