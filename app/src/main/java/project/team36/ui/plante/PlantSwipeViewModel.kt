package project.team36.ui.plante

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plante.PlantRepository
import project.team36.model.plante.PlanteInfo

class PlantSwipeViewModel(private val repository: PlantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<PlanteInfo>>(emptyList())
    val uiState: StateFlow<List<PlanteInfo>> = _uiState.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        _uiState.value = repository.getAllPlants()
    }

    fun onLike() {
        removeTopPlant()
    }

    fun onDislike() {
        removeTopPlant()
    }

    private fun removeTopPlant() {
        if (_uiState.value.isNotEmpty()) {
            _uiState.value = _uiState.value.drop(1)
        }
    }
}