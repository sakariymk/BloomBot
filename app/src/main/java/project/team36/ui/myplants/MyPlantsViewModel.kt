package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo
import project.team36.ui.map.SavedPlace
import project.team36.ui.map.SavedPlacesViewModel
import java.util.concurrent.TimeUnit

class MyPlantsViewModel(
    private val repository: LocationRepository,
    savedPlacesViewModel: SavedPlacesViewModel,
) : ViewModel() {

    //Flyttet disse til FindPlantsViewModel, gjenstår å se om det ødelegger appen.. -T
//    private val _pendingPlant = MutableStateFlow<PlantInfo?>(null)
//    val pendingPlant: StateFlow<PlantInfo?> = _pendingPlant.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val plants = savedPlacesViewModel.selectedLocation
        .flatMapLatest { location ->
            repository.getPlantsForLocation(location?.id ?: -1)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allPlants: StateFlow<List<SavedPlant>> =
        repository.getAllPlants()
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
        return daysSince >= plant.maxDaysWithoutWater
    }

    fun daysUntilWatering(plant: SavedPlant): Long {
        val daysSince = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastWateredDate
        )
        return (plant.maxDaysWithoutWater.toLong() - daysSince).coerceAtLeast(0)
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
}