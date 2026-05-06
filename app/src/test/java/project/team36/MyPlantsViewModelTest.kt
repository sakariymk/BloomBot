package project.team36

import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.util.concurrent.TimeUnit


class MyPlantsViewModelTest {

    val fakeLocationRepo : LocationRepository = mockk<LocationRepository>(relaxed = true)
    val fakeSavedPlacesViewModel = mockk<SavedPlacesViewModel>(relaxed = true)
    val fakeFrostRepo : FrostRepository = mockk<FrostRepository>(relaxed = true)

    @Test
    fun testDaysUntilWatering () {

        val viewModel = MyPlantsViewModel(fakeLocationRepo, fakeSavedPlacesViewModel,fakeFrostRepo)

        val now = System.currentTimeMillis()
        val daysAgo = 2L
        val plant = opprettTestPlante().copy(
            lastWateredDate = now - TimeUnit.DAYS.toMillis(daysAgo),
            maxDaysWithoutWater = 3
        )

        val result = viewModel.daysUntilWatering(plant)

        println("----Test result----")
        println("Dager til vannig: $result")
        assert(result == 1L) {"Forventet 1 dag til vanning, testresultat: $result"}
    }

    @Test
    fun deletingPlantWithRemovePlantByName() = runTest {

        val plant = opprettTestPlante()

        // every-lambdaene sørger for at man ikke får feil i kall til LocationRepository
        every { fakeSavedPlacesViewModel.selectedLocation } returns MutableStateFlow(null) // returnerer alltid null
        every { fakeLocationRepo.getPlantsForLocation(any()) } returns flowOf(listOf(plant)) // mocker at SavedPlants-objektet ligger i databasen for en hvilken som helst locationId

        val myPlantsViewModel = MyPlantsViewModel(fakeLocationRepo, fakeSavedPlacesViewModel,fakeFrostRepo)

        myPlantsViewModel.removePlantByName("Pelargonia")

        println("----Test result----")
        println("deletePlantByName ble kalt med parameter: ${plant.name}")
        coVerify { fakeLocationRepo.deletePlant(plant) } // sjekker at deletePlant-metode kalles under testen, med riktig SavedPlant objekt
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