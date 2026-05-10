package project.team36

import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class MyPlantsViewModelTest {

    val fakeLocationRepo : LocationRepository = mockk<LocationRepository>(relaxed = true)
    val fakeLocationsViewModel = mockk<LocationsViewModel>(relaxed = true)
    val fakeFrostRepo : FrostRepository = mockk<FrostRepository>(relaxed = true)

    @Test
    fun testDaysUntilWatering () {

        val viewModel = MyPlantsViewModel(fakeLocationRepo, fakeLocationsViewModel,fakeFrostRepo)

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
    fun deletingPlantWithRemovePlantByName() = runBlocking {
        val plant = opprettTestPlante()

        // every-lambdaene sørger for at man ikke får feil i kall til LocationRepository
        every { fakeLocationsViewModel.selectedLocation } returns MutableStateFlow(null) // returnerer alltid null
        every { fakeLocationRepo.getPlantsForLocation(any()) } returns flowOf(listOf(plant)) //mocker at SavedPlants-objektet ligger i databasen for en hvilken som helst locationId

        val myPlantsViewModel = MyPlantsViewModel(fakeLocationRepo, fakeLocationsViewModel, fakeFrostRepo)

        myPlantsViewModel.plants.filter { it.isNotEmpty() }.first() //venter til testplanten er lastet inn i viewmodel
        myPlantsViewModel.removePlantByName("Pelargonia")

        coVerify { fakeLocationRepo.deletePlant(plant) } //bekrefter  at deletePlant blir kalt med riktig plante
    }

    private fun opprettTestPlante() = SavedPlant(
        id = 1,
        locationId = 1,
        name = "Pelargonia",
        soil = setOf(SoilType.SAND),
        light = setOf(LightType.FULL),
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