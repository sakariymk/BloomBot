package project.team36.ui.map


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant


//Flyttet savedPlaces til en egen viewmodel, så flere skjermer har tilgang til lagrede steder

class SavedPlacesViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    //Begynner å lytte etter locations fra Room, Flow sørger for at det blir automatisk oppdatert
    val savedLocations: StateFlow<List<SavedLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Location valgt
    private val _selectedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedLocation: StateFlow<SavedLocation?> = _selectedLocation.asStateFlow()

    //Velg location, som kan brukes av dropdown osv
    fun selectLocation(location: SavedLocation) {
        _selectedLocation.value = location
    }

    //Legg til Location
    fun addLocation(location: SavedLocation){
        viewModelScope.launch {
            repository.insertLocation(location)
        }
    }

    //Slett location
    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    //Oppdater location
    fun updateLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.updateLocation(location)
        }
    }
}

class SavedPlacesViewModelFactory(
    private val repository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create (modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedPlacesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedPlacesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown VievModel class")
    }
}