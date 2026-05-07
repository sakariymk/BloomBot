package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Test
import project.team36.model.location.SavedLocation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import project.team36.data.local.LocationRepository
import project.team36.ui.map.SavedPlacesViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


class SavedPlacesViewModelTest {
    private val mockRepo = mockk<LocationRepository>(relaxed = true)
    private fun testLocation() = SavedLocation(
        name = "TestHage",
        lat = 60.0,
        lon = 10.0,
        address = "Gateveien",
        zone = 1
    )

    @Test
    fun testCanAddMoreLocations(): Unit = runBlocking {
        every { mockRepo.allLocations } returns flowOf(emptyList()) //simulerer at savedlocations returnerer tom liste
        val viewModel = SavedPlacesViewModel(mockRepo)

        assertTrue("Should be able to add a location", viewModel.onAddLocationClicked())
        assertFalse("ShowMaxLocationsDialog should be false", viewModel.showMaxLocationsDialog.value)
    }


    @Test
    fun testCannotAddMoreLocations(): Unit = runBlocking {
        every { mockRepo.allLocations } returns flowOf(List(5) { testLocation() }) //simulerer at det ligger 5 steder i savedlocations
        val viewModel = SavedPlacesViewModel(mockRepo)
        viewModel.savedLocations.filter {it.size == 5}.first() //filter gjør at man venter til staflow har fått 5 lokasjoner

        assertFalse("Should not be able to add a location", viewModel.onAddLocationClicked())
        assertTrue("ShowMaxLocationsDialog should be true", viewModel.showMaxLocationsDialog.value)
    }
}