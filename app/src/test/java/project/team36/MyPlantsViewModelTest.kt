package project.team36

import io.mockk.MockKAnnotations
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi


class MyPlantsViewModelTest {

    val fakeLocationRepo : LocationRepository = mockk<LocationRepository>(relaxed = true)
    val fakeSavedPlacesViewModel = mockk<SavedPlacesViewModel>(relaxed = true)
    val fakeFrostRepo : FrostRepository = mockk<FrostRepository>(relaxed = true)
    val viewModel = MyPlantsViewModel(fakeLocationRepo, fakeSavedPlacesViewModel,fakeFrostRepo)

    @Test
    fun deletingPlantWithRemovePlant() {
        val plant = opprettTestPlante()

        viewModel.removePlant(plant)

        coVerify { fakeLocationRepo.deletePlant(plant) }
    }

    private fun opprettTestPlante() = SavedPlant(
        id = 1,
        locationId = 1,
        name = "Pelargonia",
        soil = setOf(SoilType.SAND),
        light = setOf(LightType.FULL),
        isFrostResistant = false,
        waterPerWeek = 2f,
        plantingMonth = setOf(5,6),
        climateZone = 3,
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 3,
        imageRes = 0,
        description = "Pelargonia testplante"
    )

}