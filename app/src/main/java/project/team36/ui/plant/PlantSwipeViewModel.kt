package project.team36.ui.plant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plant.PlantRepository
import project.team36.model.plant.PlantInfo

class PlantSwipeViewModel(private val repository: PlantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<PlantInfo>>(emptyList())
    val uiState: StateFlow<List<PlantInfo>> = _uiState.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        _uiState.value = repository.initiatePlants()
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