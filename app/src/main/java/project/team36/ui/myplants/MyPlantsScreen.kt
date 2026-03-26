package project.team36.ui.myplants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import project.team36.ui.map.MapViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.team36.ui.map.SavedPlace
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.PlantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen (
    mapViewModel: MapViewModel,
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel
) {

    // for å få tak i liste over lagrede steder fra MapViewModel
    var selectedPlace by remember { mutableStateOf<SavedPlace?>(null) }
    val savedPlaces by savedPlacesViewModel.savedPlaces.collectAsState()
    val uiState by mapViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine planter") }
            )
        },
        bottomBar = {
            NavigationBar{
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map")},
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Kart")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Mine planter")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("findPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Oppdag planter")}
                )
//                NavigationBarItem(
//                    selected = false,
//                    onClick = { navController.navigate("swipe") },
//                    icon = { }, //fyller ut med icon senere!
//                    label = { Text("test")}
//                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PlaceDropdown(
                    selectedPlace = selectedPlace,
                    savedPlaces = savedPlaces,
                    onPlaceSelected = { selectedPlace = it },
                )
            }

            item {
                //PlantCard(title = "Tomat")
            }
            item {
                //PlantCard(title = "Basilikum")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDropdown(
    savedPlaces: List<SavedPlace>,
    selectedPlace: SavedPlace?,
    onPlaceSelected: (SavedPlace) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded } // åpner/lukker menyen
    ) {
        TextField(
            value = selectedPlace?.customName ?: "Velg sted",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            savedPlaces.forEach { place ->
                DropdownMenuItem(
                    text = { Text(place.customName) },
                    onClick = {
                        onPlaceSelected(place) // sender valgt sted til ViewModel
                        expanded = false
                    }
                )
            }
        }
    }
}
