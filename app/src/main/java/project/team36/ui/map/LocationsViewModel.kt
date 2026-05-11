package project.team36.ui.map


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation


//Flyttet savedPlaces til en egen viewmodel, så flere skjermer har tilgang til lagrede steder

class LocationsViewModel(
    repository: LocationRepository
) : ViewModel() {

    //Begynner å lytte etter locations fra Room, Flow sørger for at det blir automatisk oppdatert
    val savedLocations: StateFlow<List<SavedLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Location valgt
    private val _selectedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedLocation: StateFlow<SavedLocation?> = _selectedLocation.asStateFlow()

    private val _showMaxLocationsDialog = MutableStateFlow(false)
    val showMaxLocationsDialog: StateFlow<Boolean> = _showMaxLocationsDialog.asStateFlow()

    //Velg location, som kan brukes av dropdown osv
    fun selectLocation(location: SavedLocation) {
        _selectedLocation.value = location
    }


    //endre navn på lokasjon
    fun updateSelectedLocationName(newName: String) {
        _selectedLocation.update { it?.copy(name = newName) }
    }

    //Sjekke om det er mulig å legge til flere steder, grense er på 5
    fun onAddLocationClicked(): Boolean {
        return if (savedLocations.value.size >= 5) {
            _showMaxLocationsDialog.value = true
            false
        } else {
            true
        }
    }

    //dismisse lokasjon dialogboks
    fun dismissMaxLocationsDialog() {
        _showMaxLocationsDialog.value = false
    }
}

class LocationsViewModelFactory(
    private val repository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create (modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown VievModel class")
    }
}