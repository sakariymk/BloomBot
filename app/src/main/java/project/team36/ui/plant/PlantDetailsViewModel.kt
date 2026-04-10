package project.team36.ui.plant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plant.PlantRepository
import project.team36.model.plant.PlantInfo

class PlantDetailsViewModel (
    private val plantRepository: PlantRepository
) : ViewModel() {
    private val _plant = MutableStateFlow<PlantInfo?>(null)
    val plant : StateFlow<PlantInfo?> = _plant.asStateFlow() // offentlig read-only state, observeres av UI

    fun loadPlant(plantName: String) {
        _plant.value = plantRepository.initiatePlants().find { it.name == plantName } // henter listen av planter, går gjennom og returnerer planten som matcher plantName
    }
}