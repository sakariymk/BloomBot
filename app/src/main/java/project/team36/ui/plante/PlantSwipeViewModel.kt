package project.team36.ui.plante

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plante.PlantRepository
import project.team36.data.plante.PlantDataSource
import project.team36.model.plante.PlantInfo

class PlantSwipeViewModel(private val dtsource: PlantDataSource) : ViewModel() {

    private val _uiState = MutableStateFlow<List<PlantInfo>>(emptyList())
    val uiState: StateFlow<List<PlantInfo>> = _uiState.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        _uiState.value = dtsource.fetchPlants()
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