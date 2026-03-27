package project.team36.ui.findplants

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.model.plant.PlantInfo
import project.team36.data.plant.PlantRepository

class FindPlantsViewModel (private val plantRepository: PlantRepository) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val plants: StateFlow<List<PlantInfo>> = _plants.asStateFlow()

    init {
        _plants.value = plantRepository.initiatePlants()
    }

    fun removePlant(plant: PlantInfo) {
        _plants.value -= plant
    }
}