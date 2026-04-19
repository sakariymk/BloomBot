package project.team36.ui.findplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mapbox.maps.extension.style.expressions.dsl.generated.image
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.SimplePlantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen(
    findPlantsViewModel: FindPlantsViewModel,
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    locationName: String
) {

    val plants by findPlantsViewModel.plants.collectAsState()
    val pendingPlant by findPlantsViewModel.pendingPlant.collectAsState()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsState()
    var showSwitchLocationDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Finn nye planter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF2E7D32)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E2)
                )
            )
        },
        bottomBar = {
            Column {

                NavigationBar(containerColor = Color.Transparent) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("map") },
                        icon = { },
                        label = { Text("Kart") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("myPlaces") },
                        icon = { },
                        label = { Text("Mine steder") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { navController.navigate("findPlants/$locationName") },
                        icon = { },
                        label = { Text("Oppdag planter") }
                    )
                }
            }
        }

    ) { innerPadding ->
        Box(modifier = Modifier
                .padding(innerPadding)
                .background(Color(0xFFE8F5E2))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(plants) { plant ->
                    SimplePlantCard(
                        plant = plant,
                        onNavigateToPlantDetails = { navController.navigate("plantDetailsScreen/${plant.name}/add") }
                    )
                }
            }

// Chip med stedsnavn
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFF2D5A27))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Sted", tint = Color(0xFF2D5A27))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(locationName, color = Color(0xFF2D5A27))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = { showSwitchLocationDialog = true },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Endre sted") },
                    text = { Text("Endre sted") },
                    containerColor = Color(0xFF2D5A27),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                )
            }
        }
    }

    pendingPlant?.let { plant ->
        SavePlantDialog(
            plant = plant,
            savedLocations = savedLocations,
            onConfirm = { location -> findPlantsViewModel.confirmAddPlant(plant, location) },
            onDismiss = { findPlantsViewModel.dismissAddPlant() }
        )
    }

    if (showSwitchLocationDialog) {
        SwitchLocationDialog(
            savedLocations = savedLocations,
            currentLocationName = locationName,
            onConfirm = { location ->
                savedPlacesViewModel.selectLocation(location)
                showSwitchLocationDialog = false
                navController.navigate("findPlants/${location.name}")
            },
            onDismiss = { showSwitchLocationDialog = false }
        )
    }
}