package project.team36.ui.map

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


//Flyttet savedPlaces til en egen viewmodel, så flere skjermer har tilgang til lagrede steder

class SavedPlacesViewModel : ViewModel() {

    private val _savedPlaces = MutableStateFlow<List<SavedPlace>>(emptyList())
    val savedPlaces : StateFlow<List<SavedPlace>> = _savedPlaces.asStateFlow()


    fun addPlace(place: SavedPlace) {
        _savedPlaces.value = _savedPlaces.value + place
    }

    fun removePlace(place: SavedPlace) {
        _savedPlaces.value = _savedPlaces.value - place
    }

    fun updatePlace(place: SavedPlace, newName: String) {
        _savedPlaces.value = _savedPlaces.value.map {
            if (it == place) it.copy(customName = newName) else it
        }
    }
}