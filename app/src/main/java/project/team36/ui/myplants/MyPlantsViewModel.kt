package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.ui.map.LocationsViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.collections.filter


class MyPlantsViewModel(
    private val repository: LocationRepository,
    locationsViewModel: LocationsViewModel,
    private val frostRepository: FrostRepository
) : ViewModel() {

    private val _precipitationPerPlant = MutableStateFlow<Map<Long, Map<String, Double>>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val plants = locationsViewModel.selectedLocation
        .flatMapLatest { location ->
            repository.getPlantsForLocation(location?.id ?: -1)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allPlants: StateFlow<List<SavedPlant>> =
        repository.getAllPlants()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sletter plante basert på navn, brukes i AppNavHost på PlantDetailsScreen
    fun removePlantByName(plantName: String) {
        viewModelScope.launch {
            val plant = plants.value.find { it.name == plantName }
            plant?.let { repository.deletePlant(it) }
        }
    }

    //Metode for vanning og å sjekke om den trenger vann
    fun markAsWatered(plant: SavedPlant) {
        viewModelScope.launch {
            repository.updatePlant(
                plant.copy(
                    hasBeenWatered = true,
                    daysSinceWatered = 0,
                    lastWateredDate = System.currentTimeMillis()
                )
            )
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
        val precipitationForPlant = _precipitationPerPlant.value[plant.id] ?: emptyMap()

        return !relevantDays.any { day ->
            (precipitationForPlant[day] ?: 0.0) >= 5.0
        }
    }

    fun daysUntilWatering(plant: SavedPlant): Long {
        val daysSince = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastWateredDate
        )
        return (plant.maxDaysWithoutWater.toLong() - daysSince).coerceAtLeast(0)
    }

    fun daysSinceFertilizing(plant: SavedPlant): Long {
        val daysSince = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastFertilizationDate
        )
        return daysSince
    }

    fun markAsFertilized(plant: SavedPlant) {
        viewModelScope.launch {
            repository.updatePlant(
                plant.copy(
                    lastFertilizationDate = System.currentTimeMillis()
                )
            )
        }
    }

    //Slett location
    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    // Endre navn på location
    fun renameLocation(location: SavedLocation, newName: String) {
        viewModelScope.launch {
            repository.updateLocation(location.copy(name = newName))
        }
    }

    fun getWateringText(plant: SavedPlant): String {
        val times = plant.waterPerWeek

        return when {
            times <= 1f -> "1 gang per uke"
            times <= 2f -> "1–2 ganger per uke"
            times <= 4f -> "2–4 ganger per uke"
            else -> "Flere ganger per uke"
        }
    }

    fun loadPrecipitationForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = mutableMapOf<Long, Map<String, Double>>()

            plants.value.forEach { plant ->
                if (plant.lastWateredDate == 0L) return@forEach

                val rainPerDay = frostRepository.getPrecipitationSinceLastUsed(
                    lat = lat,
                    lon = lon,
                    lastWatered = plant.lastWateredDate.toIso8601()
                )
                if (rainPerDay != null) {
                    result[plant.id] = rainPerDay

                    val hasRainedEnough= rainPerDay.values.any { it >= 5 }
                    if (hasRainedEnough) {
                        markAsWatered(plant)
                    } else {
                        repository.updatePlant(plant.copy(hasBeenWatered = false))
                    }
                }
            }
            _precipitationPerPlant.value = result
        }
    }

    //henter alle planter som trenger vann for hver lokasjon
    fun getPlantsNeedingWaterByLocation(locations: List<SavedLocation>): Flow<Map<SavedLocation, List<Pair<SavedPlant, String>>>> {
        return allPlants.map { plants ->
            locations.associateWith { location ->
                plants
                    .filter { plant ->
                        plant.locationId == location.id && needsWater(plant) //filtrerer på de plantene som trenger vann
                    }
                    .map { plant -> plant to getWateringText(plant) } //henter også hvor ofte en plante må vannes
            }
        }
    }

    //For å få riktig tidsformat fra siste vanningsdato lagret for hver plante
    fun Long.toIso8601(): String = Instant.ofEpochMilli(this).toString()

}

class MyPlantsViewModelFactory(
    private val repository: LocationRepository,
    private val locationsViewModel: LocationsViewModel,
    private val frostRepository: FrostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPlantsViewModel(repository, locationsViewModel, frostRepository) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}