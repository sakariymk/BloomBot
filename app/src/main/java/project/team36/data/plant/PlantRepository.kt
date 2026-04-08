package project.team36.data.plant

import project.team36.model.plant.PlantInfo

class PlantRepository(private val plantDataSource: PlantDataSource) {
    private var cachedPlants: List<PlantInfo> = emptyList()


    fun initiatePlants(): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        return cachedPlants
    }

}
