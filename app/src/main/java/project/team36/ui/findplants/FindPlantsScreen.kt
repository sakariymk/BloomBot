package project.team36.ui.findplants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen(
    findPlantsViewModel: FindPlantsViewModel,
    navController: NavController,
    myPlantsViewModel: MyPlantsViewModel,
) {

    val plants by findPlantsViewModel.plants.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oppdag planter") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Kart") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Mine planter") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("findPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Oppdag planter") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plants) {plant ->
                PlantCard(
                    plant,
                    showAddButton = true,
                    onAddPlant = {
                        myPlantsViewModel.addPlant(plant)
                        findPlantsViewModel.removePlant(plant)
                    } )
            }
        }
    }
}