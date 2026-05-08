package project.team36.ui.map

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherAlert
import project.team36.model.location.SavedLocation
import project.team36.ui.map.ClimateZoneConfig.LAYER_ID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.content.Context
import java.util.Locale

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
    val weatherAlerts: List<WeatherAlert> = emptyList(),
    val locationAlerts: List<WeatherAlert> = emptyList(),
    val isAlertLoading: Boolean = false,
    val userLat: Double? = null,
    val userLon: Double? = null,
    val liveLocationPending: Boolean = false,
    val showLiveLocationConfirm: Boolean = false,
)


class MapViewModel(
    private val weatherRepository: MetWeatherRepository,
    private val savedLocationsRepository: LocationRepository
) : ViewModel() {

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

    private var hasLoadedAlerts = false
    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.SEARCH_BOX,
        SearchEngineSettings()
    )


    fun updateUserPosition(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(userLat = lat, userLon = lon)
        if (!hasLoadedAlerts) {
            hasLoadedAlerts = true
            fetchAlerts(lat, lon)
        } else {
            val currentAlerts = _uiState.value.weatherAlerts.filter { alert ->
                alert.polygons.any { polygon -> weatherRepository.isPointInPolygon(lat, lon, polygon) }
            }
            _uiState.value = _uiState.value.copy(locationAlerts = currentAlerts)
        }
        if (_uiState.value.liveLocationPending) {
            _uiState.value = _uiState.value.copy(
                liveLocationPending = false,
                showLiveLocationConfirm = true
            )
        }
    }

    fun requestLiveLocationSave() {
        val lat = _uiState.value.userLat
        val lon = _uiState.value.userLon
        if (lat != null && lon != null) {
            _uiState.value = _uiState.value.copy(showLiveLocationConfirm = true)
        } else {
            _uiState.value = _uiState.value.copy(liveLocationPending = true)
        }
    }


    suspend fun getAddress(lat: Double, lon: Double, context: Context): String? {
        return suspendCoroutine { continuation ->
            val geocoder = Geocoder(context, Locale.getDefault())
            geocoder.getFromLocation(lat, lon, 1) { results ->
                continuation.resume(results.firstOrNull()?.getAddressLine(0))
            }
        }
    }

    fun confirmSaveLiveLocation(context: Context, onSaved: () -> Unit) {
        val lat = _uiState.value.userLat ?: return
        val lon = _uiState.value.userLon ?: return
        viewModelScope.launch {
            val address = getAddress(lat, lon, context)

            // Bytt fra followPuck til fast kameraposisjon for å kunne hente klimasone
            mapViewportState.flyTo(
                cameraOptions {
                    center(Point.fromLngLat(lon, lat))
                    zoom(14.0)
                }
            )
            //delayen trengs for at kartet skal rekke å laste inn klimasonelaget, da den ellers lagrer det med 0
            kotlinx.coroutines.delay(1000)

            val zone = getClimateZone(lng = lon, lat = lat)

            savedLocationsRepository.insertLocation(
                SavedLocation(
                    name = "Min posisjon",
                    lat = lat,
                    lon = lon,
                    address = address,
                    zone = zone ?: 0
                )
            )
            _uiState.value = _uiState.value.copy(showLiveLocationConfirm = false)
            onSaved()
        }
    }

    fun dismissLiveLocationConfirm() {
        _uiState.value = _uiState.value.copy(showLiveLocationConfirm = false,
            liveLocationPending = false)
    }

    fun onLocationReady() {
        if (!hasInitializedCamera) {
            hasInitializedCamera = true
            mapViewportState.transitionToFollowPuckState()
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

    private var mapboxMap: MapboxMap? = null

    fun onMapReady(map: MapboxMap) {
        mapboxMap = map
    }

    suspend fun getClimateZone(lng: Double, lat: Double): Int? = suspendCoroutine { continuation ->
        val map = mapboxMap ?: run {
            Log.e("ClimateZone", "mapboxMap er null!")
            continuation.resume(null)
            return@suspendCoroutine
        }

        val pointOnScreen = map.pixelForCoordinate(Point.fromLngLat(lng, lat))
        Log.d("ClimateZone", "Sjekker punkt: lng=$lng, lat=$lat, pixel=$pointOnScreen")

        map.queryRenderedFeatures(
            RenderedQueryGeometry(pointOnScreen),
            RenderedQueryOptions(listOf(LAYER_ID), null)
        ) { result ->
            val features = result.value
            Log.d("ClimateZone", "Antall features: ${features?.size}, første: ${features?.firstOrNull()?.queriedFeature?.feature}")
            val zone = features
                ?.firstOrNull()
                ?.queriedFeature
                ?.feature
                ?.getNumberProperty("Klasse")
                ?.toInt()
            Log.d("ClimateZone", "Sone: $zone")
            continuation.resume(zone)
        }
    }

    fun confirmSavePlace(onSaved: () -> Unit) {
        val result = _uiState.value.selectedResult ?: return
        val name = _uiState.value.placeNameInput.ifEmpty { result.name }
        viewModelScope.launch {
            val zone = getClimateZone(
                lng = result.coordinate.longitude(),
                lat = result.coordinate.latitude()
            )

            savedLocationsRepository.insertLocation(
                SavedLocation(
                    name = name,
                    lat = result.coordinate.latitude(),
                    lon = result.coordinate.longitude(),
                    address = result.fullAddress,
                    zone = zone ?: 0
                )
            )
            dismissSaveDialog()
            onSaved()
        }
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
}



class MapViewModelFactory(
    private val weatherRepository: MetWeatherRepository,
    private val savedLocationsRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(weatherRepository, savedLocationsRepository ) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}