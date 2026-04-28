package project.team36.ui.landingpage

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
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
    val backgroundColor = Color(0xFFE8F5E9)
    val tabButtonColor = Color(0xFF386633)

    val uiState by landingPageViewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val allPlants by myPlantsViewModel.allPlants.collectAsStateWithLifecycle()

    //TODO: logikken i denne bør flyttes til ViewModel, for kompleks for screen -T
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
        containerColor = backgroundColor,
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
                    text = "Ingen lagrede hager ennå.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    savedLocations.forEachIndexed { index, location ->
                        val alerts = uiState.locationAlerts[location.id] ?: emptyList()
                        val hasAlert = alerts.isNotEmpty()
                        val alertColor = alerts.firstOrNull()?.color ?: Color.Red // får å hente ut selve fargen til alerte og display samme farge på weather-kortet
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index; savedPlacesViewModel.selectLocation(location) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(40.dp)
                                .background(
                                    color = if (isSelected) tabButtonColor else Color.White,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = tabButtonColor,
                                    shape = CircleShape
                                ),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = location.name ?: "Hage",
                                        color = if (isSelected) Color.White else tabButtonColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (hasAlert) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = alertColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                val selectedLocation = savedLocations.getOrNull(selectedTabIndex) ?: savedLocations[0]
                val alertsForSelectedLocation: List<WeatherAlert> = uiState.locationAlerts[selectedLocation.id] ?: emptyList()

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (alertsForSelectedLocation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            alertsForSelectedLocation.forEach { alert ->
                                LocationAlertBanner(alert)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Værvarsel", style = MaterialTheme.typography.labelLarge)

                    WeatherCard(
                        weatherUiState = uiState.locationForecasts[selectedLocation.id],
                        isLoading = uiState.isWeatherLoading,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { landingPageViewModel.fetchAiAdvice(selectedLocation) }, // FIKSET: location -> selectedLocation
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        enabled = !uiState.isAiLoading && !uiState.isFutureAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), disabledContainerColor = Color(0xFF4CAF50), disabledContentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isAiLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("✨ Dagens råd", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { landingPageViewModel.fetchFutureAiAdvice(selectedLocation) }, // FIKSET: location -> selectedLocation
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        enabled = !uiState.isAiLoading && !uiState.isFutureAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), disabledContainerColor = Color(0xFF2E7D32), disabledContentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isFutureAiLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("🔮 Fremtids-råd", fontSize = 13.sp)
                        }
                    }

                }


                if (uiState.aiResponse != null) {
                    AlertDialog(
                        onDismissRequest = { landingPageViewModel.dismissAiAdvice() },
                        title = { Text("Hageråd fra Claude 🌱", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column {
                                    Text("Været:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text(uiState.aiResponse!!.weatherSummary)
                                }

                                if (uiState.aiResponse!!.plantAdvice.isNotEmpty()) {
                                    Column {
                                        Text("Dine planter:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        uiState.aiResponse!!.plantAdvice.forEach { advice ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("• ${advice.plantName}", fontWeight = FontWeight.SemiBold)
                                            Text(advice.advice, modifier = Modifier.padding(start = 12.dp), color = Color.DarkGray)
                                        }
                                    }
                                }

                                if (uiState.aiResponse!!.generalTasks.isNotEmpty()) {
                                    Column {
                                        Text("Hageoppgaver:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        uiState.aiResponse!!.generalTasks.forEach { task ->
                                            Text("- $task", modifier = Modifier.padding(start = 4.dp))
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { landingPageViewModel.dismissAiAdvice() }) {
                                Text("Lukk", color = Color(0xFF2E7D32))
                            }
                        }
                    )
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