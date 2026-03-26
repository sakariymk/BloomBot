package project.team36.ui.map

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.model.plant.PlantInfo


//Flyttet savedPlaces til en egen viewmodel, så flere skjermer har tilgang til lagrede steder

class SavedPlacesViewModel : ViewModel() {

    private val _savedPlaces = MutableStateFlow<List<SavedPlace>>(emptyList())
    val savedPlaces: StateFlow<List<SavedPlace>> = _savedPlaces.asStateFlow()

    fun addPlace(place: SavedPlace) {
        _savedPlaces.value = _savedPlaces.value + place
        Log.d("SavedPlaces", "Sted lagt til: ${place.customName}, totalt: ${_savedPlaces.value.size}")
    }

    fun removePlace(place: SavedPlace) {
        _savedPlaces.value = _savedPlaces.value.filter { it.id != place.id }
    }

    fun updatePlace(place: SavedPlace, newName: String) {
        _savedPlaces.value = _savedPlaces.value.map {
            if (it.id == place.id) it.copy(customName = newName) else it
        }
    }

    fun addPlantToPlace(place: SavedPlace, plant: PlantInfo) {
        _savedPlaces.value = _savedPlaces.value.map {
            if (it.id == place.id) it.copy(plants = it.plants + plant) else it
        }
    }
}