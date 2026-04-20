package project.team36.ui.landingpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherForecast
import project.team36.model.location.SavedLocation
import kotlin.jvm.java

data class LandingPageUiState(
    val locationForecasts: Map<Long, WeatherForecast?> = emptyMap(),
    val isWeatherLoading: Boolean = false
)


class LandingPageViewModel(
    private val weatherRepository: MetWeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingPageUiState())
    val uiState: StateFlow<LandingPageUiState> = _uiState.asStateFlow()

    fun loadWeatherForLocations(locations: List<SavedLocation>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true)
            val forecasts = locations.associate { location ->
                val forecast = runCatching {
                    weatherRepository.getParsedForecast(
                        location.lat ?: 0.0,
                        location.lon ?: 0.0
                    )
                }.getOrNull()
                location.id to forecast
            }
            _uiState.value = _uiState.value.copy(
                locationForecasts = forecasts,
                isWeatherLoading = false
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