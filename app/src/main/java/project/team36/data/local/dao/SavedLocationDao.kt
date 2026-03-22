package project.team36.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import project.team36.model.location.SavedLocation

@Dao
interface SavedLocationDao {
    @Insert
    suspend fun insert(location: SavedLocation): Long

    @Query("SELECT * from saved_location")
    suspend fun getAll(): List<SavedLocation>

    //For å lagre locations som kan vises på kartet
    @Query("SELECT * FROM saved_location")
    suspend fun getAllLocations(): Flow<List<SavedLocation>>
}