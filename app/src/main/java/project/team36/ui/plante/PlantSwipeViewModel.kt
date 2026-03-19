package project.team36.ui.plante

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plante.PlantRepository
import project.team36.model.plante.PlanteInfo

/**
 * FIL: PlantSwipeViewModel.kt
 * TYPE: KLASSE
 * BESKRIVELSE: Håndterer staten til plantesveipingen.
 */
class PlantSwipeViewModel(private val repository: PlantRepository) : ViewModel() {

    // En liste over planter som er igjen i "bunken"
    private val _uiState = MutableStateFlow<List<PlanteInfo>>(emptyList())
    val uiState: StateFlow<List<PlanteInfo>> = _uiState.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        // Henter alle planter fra repository ved oppstart
        _uiState.value = repository.getAllPlants()
    }

    // Kalles når man trykker på "Liker" eller sveiper til høyre
    fun onLike() {
        removeTopPlant()
    }

    // Kalles når man trykker på "Nei" eller sveiper til venstre
    fun onDislike() {
        removeTopPlant()
    }

    private fun removeTopPlant() {
        if (_uiState.value.isNotEmpty()) {
            // Fjerner den første planten i lista (den som vises øverst)
            _uiState.value = _uiState.value.drop(1)
        }
    }
}