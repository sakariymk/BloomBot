package project.team36.data.plante

import project.team36.model.plante.PlanteInfo

class PlantRepository(private val dataSource: PlantDataSource) {
    private var cachedPlants: List<PlanteInfo> = emptyList()

    fun InitiatePlants(): List<PlanteInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = dataSource.fetchPlants()
        }
        return cachedPlants
    }

}
