package project.team36.ui.myplants

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plant.PlantRepository
import project.team36.model.plant.PlantInfo

class MyPlantsViewModel : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val plants: StateFlow<List<PlantInfo>> = _plants.asStateFlow()

    //state for å vise/skjule plantevelger-dialog
    private val _showPlantPicker = MutableStateFlow(false)
    val showPlantPicker: StateFlow<Boolean> = _showPlantPicker.asStateFlow()

    //åpne dialog
    fun openPlantPicker() { _showPlantPicker.value = true }

    // lukke dialog
    fun closePlantPicker() { _showPlantPicker.value = false }

    fun addPlant(plant: PlantInfo) {
        _plants.value += plant
    }
}