package project.team36.data.plant

import project.team36.model.plante.PlantInfo
import android.content.Context

class PlantRepository(private val context: Context) {
    private var cachedPlants: List<PlantInfo> = emptyList()
    private var plantDataSource = PlantDataSource(context = context)

    fun initiatePlants(): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        return cachedPlants
    }

}
