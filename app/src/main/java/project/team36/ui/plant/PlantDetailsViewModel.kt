package project.team36.ui.plant

import androidx.lifecycle.ViewModel
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.local.LocationRepository
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantDetailsViewModel (
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _plant = MutableStateFlow<PlantDetails?>(null)
    val plant : StateFlow<PlantDetails?> = _plant.asStateFlow() // offentlig read-only state, observeres av UI

    //Planter som lastes inn fra CSV
    fun loadPlant(plantName: String) {
        _plant.value = plantRepository.initiatePlants()
            .find { it.name == plantName } // henter listen av planter, går gjennom og returnerer planten som matcher plantName
            ?.toPlantDetails()
    }

    fun loadSavedPlant(plantName: String, plants: List<SavedPlant>) {
        _plant.value = plants
            .find{ it.name == plantName }
            ?.toPlantDetails()
    }
}

//Hjelpeklasser for å lettere kobinere data fra PlantInfo og SavedPlant, så vi kan bruke et skjerm til å displaye lik informasjon.
data class PlantDetails(
    val name: String,
    val description: String,
    val imageRes: Int,
    val climateZone: Int,
    val waterPerWeek: Float,
    val isFrostResistant: Boolean,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val plantingMonth: Set<Int>,
    val maxDaysWithoutWater: Int,
    // SavedPlant-spesifikke, null hvis fra PlantInfo og csv
    val id: Long? = null,
    val locationId: Long? = null,
    val hasBeenWatered: Boolean? = null,
    val daysSinceWatered: Int? = null,
    val lastWateredDate: Long? = null,
    val lastFertilizationDate: Long? = null
)

fun PlantInfo.toPlantDetails() = PlantDetails(
    name = name,
    description = description,
    imageRes = imageRes,
    climateZone = climateZone,
    waterPerWeek = waterPerWeek,
    isFrostResistant = isFrostResistant,
    soil = soil,
    light = light,
    plantingMonth = plantingMonth,
    maxDaysWithoutWater = maxDaysWithoutWater
)

fun SavedPlant.toPlantDetails() = PlantDetails(
    name = name,
    description = description,
    imageRes = imageRes,
    climateZone = climateZone,
    waterPerWeek = waterPerWeek,
    isFrostResistant = isFrostResistant,
    soil = soil,
    light = light,
    plantingMonth = plantingMonth,
    maxDaysWithoutWater = maxDaysWithoutWater,
    lastWateredDate = lastWateredDate,
    locationId = locationId
)