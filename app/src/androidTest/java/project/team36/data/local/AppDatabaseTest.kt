package project.team36.data.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith
import project.team36.data.local.dao.SavedLocationDao
import project.team36.data.local.dao.SavedPlantDao
import android.content.Context
import androidx.room.Room
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import project.team36.model.location.SavedLocation

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest : TestCase() {

    private lateinit var db: AppDatabase
    private lateinit var locationDao: SavedLocationDao
    private lateinit var plantDao: SavedPlantDao

    @Before
    public override fun setUp() {

        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        locationDao = db.savedLocationDao()
        plantDao = db.savedPlantDao()
    }

    @After
    fun closeDb() {
        db.close()
    }



    @Test
    fun writeAndReadLocation() = runBlocking {
        val location = SavedLocation(name = "Hjem", lat = 59.8, lon = 10.9, address = "Solveien 61")
        locationDao.insert(location)
        val locations = locationDao.getAllLocations().first()
        assertTrue(locations.any { it.name == "Hjem" })
    }
}