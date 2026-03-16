package project.team36.ui.map

import androidx.lifecycle.ViewModel
import com.mapbox.search.ApiType
import android.util.Log
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val centerLng: Double = 9.30,
    val centerLat: Double = 63.59,
    val zoom: Double = 4.3,
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList()
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
                            Log.d("Suggestion[$index]", """
                                Name: ${suggestion.name}
                                Description: ${suggestion.descriptionText ?: "N/A"}
                            """.trimIndent())
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

    fun onSuggestionClicked(suggestion: SearchSuggestion) {
        _uiState.value = _uiState.value.copy(
            suggestions = emptyList(),
            searchQuery = suggestion.name
        )
    }

}