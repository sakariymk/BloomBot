package project.team36.ui.myplants

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.unit.dp
import project.team36.ui.map.SavedPlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen (
    viewModel: MyPlantsViewModel = viewModel(),
    mapViewModel: MapViewModel

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
        onExpandedChange = { expanded = it } // åpner/lukker menyen
    ) {
        TextField(
            value = selectedPlace?.customName ?: "Velg sted",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
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