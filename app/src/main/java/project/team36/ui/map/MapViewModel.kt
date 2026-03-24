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
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine


//egen dataklasse for en lagret lokasjon
data class SavedPlace(
    val result: SearchResult,
    val customName: String //custom navn som brukeren har gitt til stedet
)
data class MapUiState(
    val centerLng: Double = 9.30,
    val centerLat: Double = 63.59,
    val zoom: Double = 4.3,
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val selectedResult: SearchResult? = null, //lagrer søkeresultat som brukeren har valgt
    val showSaveDialog: Boolean = false, //kontrollerer om "Legg til mine steder?" boksen er synlig på skjermen
    val placeNameInput: String = "",
    val selectedSavedPlace: SavedPlace? = null
)


class MapViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.SEARCH_BOX,
        SearchEngineSettings()
    )

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


    //når brukeren klikker på en suggestion i søket
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
                    searchQuery = result.name, //viser adressenavnet inni searchbar
                    suggestions = emptyList() //gjør at søkelisten går vekk etter man har søkt
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
    fun confirmSavePlace(savedPlacesViewModel: SavedPlacesViewModel) {
        val result = _uiState.value.selectedResult ?: return
        val name = _uiState.value.placeNameInput.ifEmpty { result.name }
            .ifEmpty { result.name }

        val savedPlace = SavedPlace( result = result, customName = name)

        savedPlacesViewModel.addPlace(savedPlace)

        _uiState.value = _uiState.value.copy(showSaveDialog = false, placeNameInput = "")
    }

    //hvis brukeren trykker "nei" på om de vil lagre stedet
    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false, placeNameInput = "")
    }

    //når brukeren trykker på en lagret plass/pin
    fun onSavedPlaceClicked(place: SavedPlace) {
        _uiState.value = _uiState.value.copy(selectedSavedPlace = place)
    }

    fun dismissPlaceInfo() {
        _uiState.value = _uiState.value.copy(selectedSavedPlace = null)
    }
}