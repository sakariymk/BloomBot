package project.team36.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import project.team36.model.location.SavedPlant

@Dao
interface SavedPlantDao {
    @Insert
    suspend fun insert(plant: SavedPlant)

    @Delete
    suspend fun delete(plant: SavedPlant)

    @Update
    suspend fun update(plant: SavedPlant)

    //Hent alle planter for en lokasjon
    @Query("SELECT * FROM saved_plants WHERE locationId = :locationId")
    fun getPlantsForLocation(locationId: Long): Flow<List<SavedPlant>>

    //Sjekk om plante allerede finnes -- om det skullle trengs
    @Query("SELECT * FROM saved_plants WHERE locationId = :locationId AND name = :name")
    fun getPlantByLocationAndName(locationId: Long, name: String): SavedPlant?


    @Query("SELECT * FROM saved_plants")
    fun getAllPlants(): Flow<List<SavedPlant>>

}