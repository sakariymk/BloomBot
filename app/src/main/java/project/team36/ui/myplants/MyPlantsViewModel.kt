package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.ui.map.SavedPlacesViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MyPlantsViewModel(
    private val repository: LocationRepository,
    private val savedPlacesViewModel: SavedPlacesViewModel,
    private val frostRepository: FrostRepository
) : ViewModel() {

    private val _precipitationPerDay = MutableStateFlow<Map<String, Double>>(emptyMap())

    val plants = savedPlacesViewModel.selectedLocation
        .flatMapLatest { location ->
            repository.getPlantsForLocation(location?.id ?: -1)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Slette plante
    fun removePlant(plant: SavedPlant) {
        viewModelScope.launch {
            repository.deletePlant(plant)
        }
    }

    // for å slette plante basert på navn, brukes i AppNavHost på PlantDetailsScreen
    fun removePlantByName(plantName: String) {
        viewModelScope.launch {
            val plant = plants.value.find { it.name == plantName }
            plant?.let { repository.deletePlant(it) }
        }
    }

    //Metoder for vanning og å sjekke om den trenger vann, lagt til ny egenskap til SavedPlant - lastWateredDate som kan holde på nøyaktig tid for vanning
    fun markAsWatered(plant: SavedPlant) {
        viewModelScope.launch {
            repository.updatePlant(plant.copy(
                hasBeenWatered = true,
                daysSinceWatered = 0,
                lastWateredDate = System.currentTimeMillis()
            ))
        }
    }

    fun needsWater(plant: SavedPlant): Boolean {
        val daysSince = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastWateredDate
        )
        if (daysSince < plant.maxDaysWithoutWater) return false

        //Sjekk for om det har regnet nok per dag siden sist vanning, definerer det til 5mm ila en dag som terskel
        val today = LocalDate.now()
        val relevantDays = (0 until plant.maxDaysWithoutWater).map {
            today.minusDays(it.toLong()).toString()
        }
        return relevantDays.any { day ->
            (_precipitationPerDay.value[day] ?: 0.0) < 5.0
        }
    }

    fun daysUntilWatering(plant: SavedPlant): Long {
        val daysSince = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastWateredDate
        )
        return (plant.maxDaysWithoutWater.toLong() - daysSince).coerceAtLeast(0)
    }

    fun loadPrecipitationForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val lastWatered = plants.value
                .minOfOrNull { it.lastWateredDate }
                ?: return@launch

            val rainPerDay = frostRepository.getPrecipitationSinceLastUsed(
                lat = lat,
                lon = lon,
                lastWatered = lastWatered.toIso8601()
            )
            _precipitationPerDay.value = rainPerDay ?: emptyMap()
        }
    }
}

//For å få riktig tidsformat fra siste vanningsdato lagret for hver plante
fun Long.toIso8601(): String = Instant.ofEpochMilli(this).toString()