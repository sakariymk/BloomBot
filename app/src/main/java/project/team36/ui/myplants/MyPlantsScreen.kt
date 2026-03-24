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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen (
    viewModel: MyPlantsViewModel = viewModel(),
    mapViewModel: MapViewModel,
    navController: NavController
) {

    // for å få tak i liste over lagrede steder fra MapViewModel
    val uiState by mapViewModel.uiState.collectAsState()
    val savedPlaces = uiState.savedPlaces
    var selectedPlace by remember { mutableStateOf<SavedPlace?>(null) } // hardkodet, byttes ut når viewmodel er utviklet

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine pølser") }
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
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PlaceDropdown(
                    savedPlaces = savedPlaces,
                    selectedPlace = null,
                    onPlaceSelected = { selectedPlace = it }
                )
            }

            item {
                PlaceholderExpandableCard(title = "Tomat") {
                    Text("Her kan du legge info om tomaten")
                }
            }
            item {
                PlaceholderExpandableCard(title = "Basilikum") {
                    Text("Her kan du legge info om basilikum")
                }
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
                        onPlaceSelected(place) // sender valgt distrikt til ViewModel
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderExpandableCard(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            AnimatedVisibility(visible = expanded) {
                content()
            }
        }
    }
}