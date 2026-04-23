package project.team36.ui.landingpage

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
)

private fun weatherSymbolToDescription(symbol: String): String {
    return when (symbol) {
        "clearsky"         -> "Klarvær"
        "clearsky_day"     -> "Klarvær"
        "clearsky_night"   -> "Klarvær"
        "fair"             -> "Lettskyet"
        "fair_day"         -> "Lettskyet"
        "fair_night"       -> "Lettskyet"
        "partlycloudy"     -> "Delvis skyet"
        "partlycloudy_day" -> "Delvis skyet"
        "partlycloudy_night" -> "Delvis skyet"
        "cloudy"           -> "Overskyet"
        "fog"              -> "Tåke"
        "rain"             -> "Regn"
        "heavyrain"        -> "Kraftig regn"
        "lightrain"        -> "Lett regn"
        "snow"             -> "Snø"
        "sleet"            -> "Sludd"
        "thunder"          -> "Torden"
        else               -> symbol
    }
}


private fun WeatherForecast.toUiState() = WeatherUiState(
    temperature = "${temperature}°C",
    precipitation = "🌧 $precipitation mm",
    description = weatherSymbolToDescription(weatherSymbol),
    iconName = weatherSymbol
)


class LandingPageViewModel(
    private val weatherRepository: MetWeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingPageUiState())
    val uiState: StateFlow<LandingPageUiState> = _uiState.asStateFlow()


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
    private val weatherRepository: MetWeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LandingPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LandingPageViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}