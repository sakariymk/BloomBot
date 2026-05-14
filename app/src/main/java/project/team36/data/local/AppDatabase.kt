package project.team36.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import project.team36.data.local.dao.SavedLocationDao
import project.team36.data.local.dao.SavedPlantDao
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant

@Database(
    entities = [
        SavedLocation::class,
        SavedPlant::class
    ],
    version = 5,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun savedPlantDao(): SavedPlantDao

    // Singleton objekt for å opprette en database for hele appen
    companion object {
        //Fordi vi la til et nytt egenskap til SavedPlant, må databasen migreres for å ikke miste data.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE saved_plants ADD COLUMN lastWateredDate INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE saved_plants ADD COLUMN lastFertilizationDate INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        // SQLite støtter ikke drop column, så må lage ny table of kopiere over informasjonen
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
            CREATE TABLE saved_plants_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                locationId INTEGER NOT NULL,
                name TEXT NOT NULL,
                soil TEXT NOT NULL,
                light TEXT NOT NULL,
                waterPerWeek REAL NOT NULL,
                plantingMonth TEXT NOT NULL,
                climateZone INTEGER NOT NULL,
                hasBeenWatered INTEGER NOT NULL,
                daysSinceWatered INTEGER NOT NULL,
                maxDaysWithoutWater INTEGER NOT NULL,
                imageRes INTEGER NOT NULL,
                description TEXT NOT NULL,
                lastWateredDate INTEGER NOT NULL,
                lastFertilizationDate INTEGER NOT NULL,
                FOREIGN KEY(locationId) REFERENCES saved_location(id) ON DELETE CASCADE
            )
        """)
                db.execSQL("""
            INSERT INTO saved_plants_new 
            SELECT id, locationId, name, soil, light, waterPerWeek, plantingMonth, 
                   climateZone, hasBeenWatered, daysSinceWatered, maxDaysWithoutWater, 
                   imageRes, description, lastWateredDate, lastFertilizationDate
            FROM saved_plants
        """)
                db.execSQL("DROP TABLE saved_plants")
                db.execSQL("ALTER TABLE saved_plants_new RENAME TO saved_plants")
                db.execSQL("CREATE INDEX index_saved_plants_locationId ON saved_plants(locationId)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}