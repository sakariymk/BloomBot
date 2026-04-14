package project.team36.ui.myplants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel  // NY: import for viewModel()
import androidx.navigation.NavController
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.PlantCard
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    myPlantsViewModel: MyPlantsViewModel
) {

    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()
    val selectedLocation by savedPlacesViewModel.selectedLocation.collectAsStateWithLifecycle()

    //hent planter for valgt sted
    val plants by myPlantsViewModel.plants.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine planter") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map") },
                    icon = { },
                    label = { Text("Kart") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("myPlants") },
                    icon = { },
                    label = { Text("Mine planter") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("findPlants") },
                    icon = { },
                    label = { Text("Oppdag planter") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addPlant") }) {
                Icon(Icons.Default.Add, contentDescription = "Legg til plante")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LocationDropdown(
                    savedLocations = savedLocations,
                    selectedLocation = selectedLocation,
                    onLocationSelected = { savedPlacesViewModel.selectLocation(it) }
                )
            }

            // Vi mater inn riktig liste, og tegner opp nøyaktig det elementet vi er på.
            items(plants){ savedPlant ->
                PlantCard(
                    plant = savedPlant.toPlantInfo(),
                    onDeletePlant = {
                        myPlantsViewModel.removePlant(savedPlant)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    savedLocations: List<SavedLocation>,
    selectedLocation: SavedLocation?,
    onLocationSelected: (SavedLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedLocation?.name ?: "Velg sted",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            savedLocations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.name ?: "Ukjent") },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}

//For at alt skal fungere midledtidig før vi får laget nye plantsCards, eller andre løsninger
fun SavedPlant.toPlantInfo(): PlantInfo = PlantInfo(
    name = this.name,
    soil = this.soil,
    light = this.light,
    isFrostResistant = this.isFrostResistant,
    waterPerWeek = this.waterPerWeek,
    plantingMonth = this.plantingMonth,
    climateZone = this.climateZone,
    hasBeenWatered = this.hasBeenWatered,
    daysSinceWatered = this.daysSinceWatered,
    maxDaysWithoutWater = this.maxDaysWithoutWater,
    imageRes = this.imageRes,
    description = this.description
)