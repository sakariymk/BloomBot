package project.team36.ui.findplants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.navigation.BottomNavBar
import project.team36.ui.plant.PlantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen(
    findPlantsViewModel: FindPlantsViewModel,
    navController: NavController,
    myPlantsViewModel: MyPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel
) {

    val plants by findPlantsViewModel.plants.collectAsState()
    val pendingPlant by myPlantsViewModel.pendingPlant.collectAsState()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finn nye planter") }
            )
        },

        bottomBar = { BottomNavBar(navController = navController) }

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plants) {plant ->
                PlantCard(
                    plant = plant,
                    showAddButton = true,
                    onAddPlant = {
                        myPlantsViewModel.requestAddPlant(plant)
                    } )
            }
        }
    }

    pendingPlant?.let { plant ->
        SavePlantDialog(
            plant = plant,
            savedLocations = savedLocations,
            onConfirm = { location -> myPlantsViewModel.confirmAddPlant(plant, location) },
            onDismiss = { myPlantsViewModel.dismissAddPlant() }
        )
    }
}