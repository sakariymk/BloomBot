package project.team36.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val centerLng: Double = -98.0,
    val centerLat: Double = 39.5,
    val zoom: Double = 2.0
)


class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateCameraPosition(lng: Double, lat: Double, zoom: Double) {
        _uiState.value = _uiState.value.copy(
            centerLng = lng,
            centerLat = lat,
            zoom = zoom
        )
    }

}