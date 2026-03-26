package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.R
import project.team36.data.plant.PlantRepository
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class MyPlantsViewModel : ViewModel () {
    // To hardcodede planter for å vise funksjonalitet
    private val _plants = MutableStateFlow<List<PlantInfo>>(
        listOf(
            PlantInfo(
                name = "Daglilje",
                soil = setOf(SoilType.LOAM, SoilType.CLAY),
                light = setOf(LightType.FULL, LightType.PARTIAL),
                isFrostResistant = true,
                waterPerWeek = 1.5f,
                plantingMonth = setOf(4, 5),
                climateZone = 2,
                hasBeenWatered = false,
                daysSinceWatered = 0,
                maxDaysWithoutWater = 7,
                imageRes = R.drawable.defaultplant,
                description = "En hardfør hageplante som trives godt i det norske klimaet."
            ),
            PlantInfo(
                name = "Løvetann",
                soil = setOf(SoilType.LOAM, SoilType.SAND),
                light = setOf(LightType.FULL),
                isFrostResistant = true,
                waterPerWeek = 1.0f,
                plantingMonth = setOf(3, 4, 5),
                climateZone = 2,
                hasBeenWatered = false,
                daysSinceWatered = 0,
                maxDaysWithoutWater = 10,
                imageRes = R.drawable.defaultplant,
                description = "Svært hardfør plante som klarer seg i nesten alle forhold."
            )
        )
    )
    val plants: StateFlow<List<PlantInfo>> = _plants.asStateFlow()
}