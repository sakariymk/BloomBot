package project.team36.ui.myplants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import project.team36.ui.map.SavedPlace
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.PlantCard
import project.team36.model.plant.PlantInfo
import project.team36.data.plant.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantRepository: PlantRepository
) {

    // Observer savedPlaces fra ViewModel
    val savedPlaces by savedPlacesViewModel.savedPlaces.collectAsStateWithLifecycle()
    var selectedPlace by remember { mutableStateOf<SavedPlace?>(null) }
    val plantsForSelectedPlace = selectedPlace?.plants ?: emptyList()

    LaunchedEffect(savedPlaces) {
        selectedPlace = savedPlaces.find { it.id == selectedPlace?.id }
    }

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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- Dropdown for å velge sted ---
            item {
                PlaceDropdown(
                    savedPlaces = savedPlaces,
                    selectedPlace = selectedPlace,
                    onPlaceSelected = { selectedPlace = it }
                )
            }

            // --- Liste over planter for valgt sted ---
            items(plantsForSelectedPlace) { plant: PlantInfo ->
                PlantCard(plante = plant)
            }

            // --- Knapp for å legge til en plante ---
            item {
                Button(
                    onClick = {
                        selectedPlace?.let { place ->
                            val newPlant = plantRepository.InitiatePlants().firstOrNull()
                            if (newPlant != null) {
                                savedPlacesViewModel.addPlantToPlace(place, newPlant)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Legg til plante")
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
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedPlace?.customName ?: "Velg sted",
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
            savedPlaces.forEach { place ->
                DropdownMenuItem(
                    text = { Text(place.customName) },
                    onClick = {
                        onPlaceSelected(place)
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