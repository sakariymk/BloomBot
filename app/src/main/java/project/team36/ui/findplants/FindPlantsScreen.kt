package project.team36.ui.findplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.plant.SimplePlantCard
import project.team36.ui.navigation.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen(
    findPlantsViewModel: FindPlantsViewModel,
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    locationName: String
) {
    val tabButtonColor = Color(0xFF386633)

    val plants by findPlantsViewModel.plants.collectAsState()
    val notRecommendedPlants by findPlantsViewModel.notRecommendedPlants.collectAsState()
    val pendingPlant by findPlantsViewModel.pendingPlant.collectAsState()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsState()
    val selectedLocation by savedPlacesViewModel.selectedLocation.collectAsState()
    var showSwitchLocationDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anbefalte planter", "Andre planter")

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            findPlantsViewModel.loadRecommendedPlants(it)
        }
    }

    Scaffold(
        containerColor = Color(0xFFE8F5E9),
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
        bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (selectedLocation == null) {
                    Text(
                        text = "Legg til eller velg en hage for å få tilpassede planteanbefalinger🌱",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .background(
                                        color = if (isSelected) tabButtonColor else Color.White,
                                        shape = CircleShape
                                    )
                                    .border(1.dp, tabButtonColor, CircleShape),
                                text = {
                                    Text(
                                        text = title,
                                        color = if (isSelected) Color.White else tabButtonColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                    val location = selectedLocation
                    val infoText = when (selectedTabIndex) {
                        0 -> "Planter som egner seg for hagens herdighetssone og vokseforhold. Din herdighetssone for ${location?.name} er ${location?.zone}, dermed vil planter med sone 1 til ${location?.zone} trives godt her🌱"
                        1 -> "Disse plantene egner seg ikke like godt for din hage, men kan likevel trives om du har et lunt hjørne plantene kan vokse i."
                        else -> ""
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2D5A27)
                        )
                    }

                    val displayedPlants = if (selectedTabIndex == 0) plants else notRecommendedPlants

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 100.dp
                        )
                    ) {
                        items(displayedPlants) { plant ->
                            SimplePlantCard(
                                plant = plant,
                                onNavigateToPlantDetails = {
                                    navController.navigate("plantDetailsScreen/${plant.name}/add")
                                }
                            )
                        }
                    }
                }
            }

            // Chip + FAB flytende over innhold
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
                        Icon(Icons.Default.LocationOn, contentDescription = "Hage", tint = Color(0xFF2D5A27))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocation?.name ?: locationName.ifEmpty { "Ingen hage valgt" },
                            color = Color(0xFF2D5A27)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = { showSwitchLocationDialog = true },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Bytt hage") },
                    text = { Text("Bytt hage") },
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