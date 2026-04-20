package project.team36.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import project.team36.data.ai.HybridAiRepository
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherAlert
import project.team36.model.location.SavedLocation
import project.team36.model.plant.PlantInfo
import project.team36.ui.map.ClimateZoneConfig.LAYER_ID
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class SavedPlace(
    val result: SearchResult,
    val customName: String,
    val plants: List<PlantInfo> = emptyList(),
    val id: String = UUID.randomUUID().toString()

)

data class MapUiState(
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val selectedResult: SearchResult? = null,
    val showSaveDialog: Boolean = false,
    val placeNameInput: String = "",
    val selectedSavedLocation: SavedLocation? = null,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false,
    val weatherAlerts: List<WeatherAlert> = emptyList(),
    val locationAlerts: List<WeatherAlert> = emptyList(),
    val isAlertLoading: Boolean = false
)

class MapViewModel(
    private val repository: HybridAiRepository,
    private val weatherRepository: MetWeatherRepository
) : ViewModel() {

    val mapViewportState = MapViewportState().apply {
        setCameraOptions {
            zoom(4.3)
            center(Point.fromLngLat(9.30, 63.59))
        }
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var mapboxMap: MapboxMap? = null
    private var hasLoadedAlerts = false
    private val searchEngine = SearchEngine.createSearchEngine(ApiType.SEARCH_BOX, SearchEngineSettings())

    fun onMapReady(map: MapboxMap) {
        mapboxMap = map
    }

    fun updateUserPosition(lat: Double, lon: Double) {
        if (!hasLoadedAlerts) {
            hasLoadedAlerts = true
            fetchAlerts(lat, lon)
        } else {
            val currentAlerts = _uiState.value.weatherAlerts.filter { alert ->
                alert.polygons.any { polygon -> weatherRepository.isPointInPolygon(lat, lon, polygon) }
            }
            _uiState.value = _uiState.value.copy(locationAlerts = currentAlerts)
        }
    }

    fun fetchAlerts(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAlertLoading = true)
            try {
                val allAlerts = weatherRepository.getAllAlerts()
                val local = allAlerts.filter { alert ->
                    alert.polygons.any { polygon -> weatherRepository.isPointInPolygon(userLat, userLon, polygon) }
                }
                _uiState.value = _uiState.value.copy(
                    weatherAlerts = allAlerts,
                    locationAlerts = local,
                    isAlertLoading = false
                )
            } catch (e: Exception) {
                Log.e("ALERTS", "Feil ved henting av varsler: ${e.message}")
                _uiState.value = _uiState.value.copy(isAlertLoading = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 2) {
            searchEngine.search(query, SearchOptions(limit = 6), object : SearchSuggestionsCallback {
                override fun onSuggestions(suggestions: List<SearchSuggestion>, info: ResponseInfo) {
                    _uiState.value = _uiState.value.copy(suggestions = suggestions)
                }
                override fun onError(e: Exception) {}
            })
        }
    }

    fun handleSuggestionSelection(suggestion: SearchSuggestion, mapViewportState: MapViewportState) {
        searchEngine.select(suggestion, object : SearchSelectionCallback {
            override fun onResult(suggestion: SearchSuggestion, result: SearchResult, info: ResponseInfo) {
                _uiState.value = _uiState.value.copy(
                    selectedResult = result,
                    showSaveDialog = true,
                    searchQuery = result.name,
                    suggestions = emptyList()
                )
                val camera = cameraOptions { center(result.coordinate); zoom(14.0) }
                mapViewportState.flyTo(camera, MapAnimationOptions.Builder().duration(2000L).build())
            }
            override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {}
            override fun onError(e: Exception) {}
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {}
        })
    }

    suspend fun getClimateZone(lng: Double, lat: Double): Int? = suspendCoroutine { continuation ->
        val map = mapboxMap ?: run { continuation.resume(null); return@suspendCoroutine }
        val pointOnScreen = map.pixelForCoordinate(Point.fromLngLat(lng, lat))
        map.queryRenderedFeatures(
            RenderedQueryGeometry(pointOnScreen),
            RenderedQueryOptions(listOf(LAYER_ID), null)
        ) { result ->
            val zone = result.value?.firstOrNull()?.queriedFeature?.feature?.getNumberProperty("Klasse")?.toInt()
            continuation.resume(zone)
        }
    }

    fun confirmSavePlace(savedPlacesViewModel: SavedPlacesViewModel) {
        val result = _uiState.value.selectedResult ?: return
        val name = _uiState.value.placeNameInput.ifEmpty { result.name }
        viewModelScope.launch {
            val zone = getClimateZone(result.coordinate.longitude(), result.coordinate.latitude())
            savedPlacesViewModel.addLocation(SavedLocation(
                name = name,
                lat = result.coordinate.latitude(),
                lon = result.coordinate.longitude(),
                address = result.fullAddress,
                zone = zone ?: 0
            ))
            dismissSaveDialog()
        }
    }

    fun dismissSaveDialog() { _uiState.value = _uiState.value.copy(showSaveDialog = false) }
    fun onSavedLocationClicked(location: SavedLocation) { _uiState.value = _uiState.value.copy(selectedSavedLocation = location) }
    fun dismissPlaceInfo() { _uiState.value = _uiState.value.copy(selectedSavedLocation = null) }
    fun fetchAiAdvice(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(isAiLoading = true)
        viewModelScope.launch {
            val response = repository.getPlantRecommendations(lat, lon)
            _uiState.value = _uiState.value.copy(isAiLoading = false, aiResponse = response)
        }
    }
    fun dismissAiAdvice() { _uiState.value = _uiState.value.copy(aiResponse = null) }
}