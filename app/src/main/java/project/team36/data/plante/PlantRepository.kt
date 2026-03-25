package project.team36.data.plante

import project.team36.model.plante.PlantInfo

class PlantRepository(private val dataSource: PlantDataSource) {
    private var cachedPlants: List<PlantInfo> = emptyList()


    fun InitiatePlants(): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = dataSource.fetchPlants()
        }
        return cachedPlants
    }

}
