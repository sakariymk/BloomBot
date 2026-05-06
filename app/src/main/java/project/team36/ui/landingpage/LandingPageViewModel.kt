package project.team36.ui.landingpage

import android.R.attr.src
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherAlert
import project.team36.data.mcp.WeatherForecast
import project.team36.model.location.SavedLocation
import kotlin.jvm.java
import project.team36.data.ai.HybridAiRepository
import project.team36.model.ai.AiRecommendationResponse




data class WeatherUiState(
    val temperature: String,
    val precipitation: String,
    val description: String,
    val iconName: String
)

data class LandingPageUiState(
    val locationForecasts: Map<Long, WeatherUiState?> = emptyMap(),
    val isWeatherLoading: Boolean = false,
    val locationAlerts : Map <Long, List<WeatherAlert>> = emptyMap(),
    val isAlertsLoading : Boolean = false,
    val aiResponse: AiRecommendationResponse? = null,
    val isAiLoading: Boolean = false,
    val isFutureAiLoading: Boolean = false
)

private fun weatherSymbolToDescription(symbol: String): String {
    return when (symbol) {
        "clearsky", "clearsky_day", "clearsky_night", "clearsky_polartwilight" -> "Klarvær"
        "fair", "fair_day", "fair_night", "fair_polartwilight" -> "Lettskyet"
        "partlycloudy", "partlycloudy_day", "partlycloudy_night" -> "Delvis skyet"
        "cloudy" -> "Overskyet"
        "fog" -> "Tåke"
        "rain" -> "Regn"
        "heavyrain" -> "Kraftig regn"
        "lightrain" -> "Lett regn"
        "snow" -> "Snø"
        "sleet", "heavysleet" -> "Sludd"
        "thunder" -> "Torden"
        "heavyrainandthunder" -> "Kraftig regn og torden"

        "sleetshowers_day" -> "Sluddbyger"

        "heavyrainshowers_day",
        "heavyrainshowers",
        "heavyrainshowers_night",
        "heavyrainshowers_polartwilight" -> "Kraftige regnbyger"

        "heavysleetandthunder",
        "heavyrainshowersandthunder_night",
        "heavyrainshowersandthunder_day",
        "heavyrainshowersandthunder_polartwilight" -> "Tordenbyger"
        else -> symbol
    }
}


private fun WeatherForecast.toUiState() = WeatherUiState(
    temperature = "${temperature}°C",
    precipitation = "🌧 $precipitation mm",
    description = weatherSymbolToDescription(weatherSymbol),
    iconName = weatherSymbol
)


class LandingPageViewModel(
    private val weatherRepository: MetWeatherRepository,
    private val aiRepository: HybridAiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingPageUiState())
    val uiState: StateFlow<LandingPageUiState> = _uiState.asStateFlow()

    fun fetchAiAdvice(location: SavedLocation) {
        _uiState.value = _uiState.value.copy(isAiLoading = true, aiResponse = null)
        viewModelScope.launch {
            val response = aiRepository.getPlantRecommendations(location)
            _uiState.value = _uiState.value.copy(isAiLoading = false, aiResponse = response)
        }
    }

    fun fetchFutureAiAdvice(location: SavedLocation) {
        _uiState.value = _uiState.value.copy(isFutureAiLoading = true, aiResponse = null)
        viewModelScope.launch {
            val response = aiRepository.getFuturePlantRecommendations(location)
            _uiState.value = _uiState.value.copy(isFutureAiLoading = false, aiResponse = response)
        }
    }

    fun dismissAiAdvice() {
        _uiState.value = _uiState.value.copy(aiResponse = null)
    }

    fun loadDataForLocations(locations: List<SavedLocation>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true, isAlertsLoading = true)

            // Henter alle varsler
            val allAlerts = runCatching { weatherRepository.getAllAlerts() }.getOrDefault(emptyList())

            val alertsMap = mutableMapOf<Long, List<WeatherAlert>>()
            val forecastsMap = mutableMapOf<Long, WeatherUiState?>()

            locations.forEach { location ->
                val lat = location.lat ?: 0.0
                val lon = location.lon ?: 0.0

                // Filtrer varsler som treffer akkurat denne lokasjonen
                val relevantAlerts = allAlerts.filter { alert ->
                    alert.polygons.any { polygon ->
                        weatherRepository.isPointInPolygon(lat, lon, polygon)
                    }
                }
                alertsMap[location.id] = relevantAlerts

                // Hent værvarsel
                val forecast = runCatching {
                    weatherRepository.getParsedForecast(lat, lon)
                }.getOrNull()
                forecastsMap[location.id] = forecast?.toUiState()
            }

            _uiState.value = _uiState.value.copy(
                locationForecasts = forecastsMap,
                locationAlerts = alertsMap,
                isWeatherLoading = false,
                isAlertsLoading = false
            )
        }
    }
}

class LandingPageViewModelFactory(
    private val weatherRepository: MetWeatherRepository,
    private val aiRepository: HybridAiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LandingPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LandingPageViewModel(weatherRepository, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}