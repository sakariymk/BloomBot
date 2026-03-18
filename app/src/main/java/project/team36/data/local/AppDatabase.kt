package project.team36.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import project.team36.data.local.dao.SavedLocationDao
import project.team36.data.local.dao.SavedPlantDao
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant

@Database(
    entities = [
        SavedLocation::class,
        SavedPlant::class
    ],
    version = 1
)

@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun savedPlantDao(): SavedPlantDao
}