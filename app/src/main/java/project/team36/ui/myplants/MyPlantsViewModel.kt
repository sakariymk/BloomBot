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
    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())

    //funksjon for å legge til plante fra oppdagsiden
    fun addPlant(plant: PlantInfo){
        _plants.value += plant
    }
    val plants: StateFlow<List<PlantInfo>> = _plants.asStateFlow()
}