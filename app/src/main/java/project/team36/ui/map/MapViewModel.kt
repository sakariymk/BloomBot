package project.team36.ui.map

import androidx.lifecycle.ViewModel
import android.util.Log
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.observable.model.Response
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine

data class MapUiState(
    val centerLng: Double = 9.30,
    val centerLat: Double = 63.59,
    val zoom: Double = 4.3,
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),

    val selectedResult: SearchResult? = null, //lagrer søkeresultat som brukeren har valgt
    val savedPlaces: List<SearchResult> = emptyList(), //liste med lagrede steder

    //kontrollerer om "Legg til mine steder?" boksen er synlig på skjermen
    val showSaveDialog: Boolean = false

)


class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.SEARCH_BOX,
        SearchEngineSettings()
    )


    fun updateCameraPosition(lng: Double, lat: Double, zoom: Double) {
        _uiState.value = _uiState.value.copy(
            centerLng = lng,
            centerLat = lat,
            zoom = zoom
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.length >= 2) {
            searchEngine.search(
                query,
                SearchOptions(limit = 10),
                object : SearchSuggestionsCallback {
                    override fun onSuggestions(
                        list: List<SearchSuggestion>,
                        responseInfo: ResponseInfo
                    ) {
                        _uiState.value = _uiState.value.copy(suggestions = list)
                        list.forEachIndexed { index, suggestion ->
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
                    showSaveDialog = true, //trigger den boksen i UI
                    searchQuery = result.name //viser adressenavnet inni searchbar
                )

                //kameraet flyr til valgt lokasjon
                val camera = cameraOptions {
                    center(result.coordinate)
                    zoom(14.0)
                }
                val animationOptions = MapAnimationOptions.Builder()
                    .duration(2500L)
                    .build()
                mapViewportState.flyTo(camera, animationOptions)

            }

            //tomme funksjoner bare fordi SearchSelectionCallback krever å implementere disse(siden den er et interface)
            override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {}
            override fun onError(e: Exception) {}
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {}
        })
    }

    //hvis brukeren trykker "ja" på om de vil lagre stedet
    fun confirmSavePlace() {
        val result = _uiState.value.selectedResult ?: return

        _uiState.value = _uiState.value.copy(
            savedPlaces = _uiState.value.savedPlaces + result, //legger til resultatet i savedPlaces
            showSaveDialog = false //lukker den dialog boksen
        )
    }

    //hvis brukeren trykker "nei" på om de vil lagre stedet
    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(
            showSaveDialog = false)
    }
}