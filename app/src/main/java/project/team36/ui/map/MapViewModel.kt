package project.team36.ui.map

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import project.team36.data.ai.HybridAiRepository
import project.team36.model.plant.PlantInfo
import java.util.UUID
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.launch
import project.team36.model.location.SavedLocation

data class SavedPlace(
    val result: SearchResult,
    val customName: String,
    val plants: List<PlantInfo> = emptyList(),
    val id: String = UUID.randomUUID().toString()

)

data class MapUiState(
    val centerLng: Double = 9.30,
    val centerLat: Double = 63.59,
    val zoom: Double = 4.3,
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val selectedResult: SearchResult? = null,
    val showSaveDialog: Boolean = false,
    val placeNameInput: String = "",
    val selectedSavedLocation: SavedLocation? = null,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false
)


class MapViewModelFactory(
    private val repository: HybridAiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



class MapViewModel(private val repository: HybridAiRepository) : ViewModel() {

    val mapViewportState = MapViewportState().apply {
        setCameraOptions {
            zoom(4.3)
            center(Point.fromLngLat(9.30, 63.59))
            pitch(0.0)
            bearing(0.0)
        }
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    private var hasInitializedCamera = false //for at kameraet skal ikke zoome sykt mye rundt når man beveger på seg

    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.SEARCH_BOX,
        SearchEngineSettings()
    )

    fun onLocationReady() {
        if (!hasInitializedCamera) {
            hasInitializedCamera = true
            mapViewportState.transitionToFollowPuckState()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.length >= 2) {
            searchEngine.search(
                query,
                SearchOptions(limit = 6),
                object : SearchSuggestionsCallback {
                    override fun onSuggestions(
                        suggestions: List<SearchSuggestion>,
                        responseInfo: ResponseInfo
                    ) {
                        _uiState.value = _uiState.value.copy(suggestions = suggestions)
                        suggestions.forEachIndexed { index, suggestion ->
                            Log.d(
                                "Suggestion[$index]", """
                                Name: ${suggestion.name}
                                Description: ${suggestion.descriptionText ?: "N/A"}
                            """.trimIndent()
                            )
                        }
                    }

                    override fun onError(e: Exception) {
                        Log.e("MapViewModel", "Search error", e)
                    }
                }
            )
        } else {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
        }
    }

    fun handleSuggestionSelection(
        suggestion: SearchSuggestion,
        mapViewportState: MapViewportState,
    ) {
        searchEngine.select(suggestion, object : SearchSelectionCallback {
            override fun onResult(
                suggestion: SearchSuggestion,
                result: SearchResult,
                responseInfo: ResponseInfo,
            ) {
                _uiState.value = _uiState.value.copy(
                    selectedResult = result,
                    showSaveDialog = true,
                    searchQuery = result.name,
                    suggestions = emptyList()
                )

                val camera = cameraOptions {
                    center(result.coordinate)
                    zoom(14.0)
                }
                val animationOptions = MapAnimationOptions.Builder()
                    .duration(2500L)
                    .build()
                mapViewportState.flyTo(camera, animationOptions)
            }

            override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {}
            override fun onError(e: Exception) {}
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {}
        })
    }

    //Lagre stedet i DB
    fun confirmSavePlace(savedPlacesViewModel: SavedPlacesViewModel) {
        val result = _uiState.value.selectedResult ?: run {
            Log.e("MapViewModel", "confirmSavePlace: selectedResult er null!")
            return
        }
        val name = _uiState.value.placeNameInput.ifEmpty { result.name }

        savedPlacesViewModel.addLocation(
            SavedLocation(
                name = name,
                lat = result.coordinate.latitude(),
                lon = result.coordinate.longitude(),
                address = result.fullAddress
            )
        )
    }

    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false, placeNameInput = "")
    }

    fun onSavedLocationClicked(location: SavedLocation) {
        _uiState.value = _uiState.value.copy(selectedSavedLocation = location)
    }

    fun dismissPlaceInfo() {
        _uiState.value = _uiState.value.copy(selectedSavedLocation = null)
    }

    fun fetchAiAdvice(lat: Double, lon: Double) {
        // Skrur på "laster..."-visningen i UI-et
        _uiState.value = _uiState.value.copy(isAiLoading = true, aiResponse = null)

        viewModelScope.launch {
            // Venter på at Claude skal tenke ferdig (og sjekke været)
            val response = repository.getPlantRecommendations(lat, lon)

            // Skrur av "laster..." og lagrer Claude sitt svar!
            _uiState.value = _uiState.value.copy(
                isAiLoading = false,
                aiResponse = response
            )
        }
    }

    // En liten funksjon for å lukke AI-svaret igjen:
    fun dismissAiAdvice() {
        _uiState.value = _uiState.value.copy(aiResponse = null)
    }
}