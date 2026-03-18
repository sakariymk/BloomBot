package project.team36.data.local.dao

import androidx.room.Insert
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant

interface SavedPlantDao {
    @Insert
    suspend fun insert(plant: SavedPlant)
    //TODO
}