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
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo
import project.team36.ui.map.SavedPlace
import project.team36.ui.map.SavedPlacesViewModel

class MyPlantsViewModel(
    private val repository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel
) : ViewModel() {

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
}