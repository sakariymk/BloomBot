package project.team36.ui.landingpage

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.navigation.BottomNavBar
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.data.mcp.WeatherAlert

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LandingPageScreen(
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    landingPageViewModel: LandingPageViewModel,
    myPlantsViewModel: MyPlantsViewModel
) {
    val uiState by landingPageViewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val allPlants by myPlantsViewModel.allPlants.collectAsStateWithLifecycle()


    val plantsByLocation = remember(savedLocations, allPlants) {
        savedLocations.associateWith { location ->
            allPlants
                .filter { plant ->
                    plant.locationId == location.id && myPlantsViewModel.needsWater(plant)
                }
                .map { plant -> plant to myPlantsViewModel.getWateringText(plant) }
        }
    }

    LaunchedEffect(Unit) { locationPermission.launchPermissionRequest() }

    LaunchedEffect(savedLocations) {
        if (savedLocations.isNotEmpty()) {
            landingPageViewModel.loadDataForLocations(savedLocations)
        }
    }

    Scaffold(
        containerColor = Color(0xFFE8F8E3),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hjem",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2E7D32)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE8F5E2))
            )
        },

        bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel) }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (savedLocations.isEmpty()) {
                Text(
                    text = "Ingen lagrede steder ennå.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    savedLocations.forEachIndexed { index, location ->
                        val hasAlert =
                            (uiState.locationAlerts[location.id] ?: emptyList()).isNotEmpty()
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(location.name ?: "Sted")
                                    if (hasAlert) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }


                // Innhold for valgt lokasjon
                val selectedLocation =
                    savedLocations.getOrNull(selectedTabIndex) ?: savedLocations[0]
                val alertsForSelectedLocation =
                    uiState.locationAlerts[selectedLocation.id] ?: emptyList()
                val forecastForSelectedLocation = uiState.locationForecasts[selectedLocation.id]

                Spacer(modifier = Modifier.height(8.dp))

                WeatherCard(
                    weatherUiState = uiState.locationForecasts[selectedLocation.id],
                    isLoading = uiState.isWeatherLoading,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (alertsForSelectedLocation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        alertsForSelectedLocation.forEach { alert ->
                            LocationAlertBanner(alert)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DailyTasksCard(
                    plantsByLocation = plantsByLocation,
                    onMarkAsWatered = { myPlantsViewModel.markAsWatered(it) },
                    navController = navController,
                )
            }
        }
    }
}

@Composable
fun LocationAlertBanner(alert: WeatherAlert) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(alert.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, alert.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = alert.color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = alert.event.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = alert.description,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}