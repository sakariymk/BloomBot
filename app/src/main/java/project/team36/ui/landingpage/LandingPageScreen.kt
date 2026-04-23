package project.team36.ui.landingpage

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import project.team36.data.mcp.WeatherAlert
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.navigation.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LandingPageScreen(
    navController: NavController,
    savedPlacesViewModel: SavedPlacesViewModel,
    landingPageViewModel: LandingPageViewModel
) {
    val uiState by landingPageViewModel.uiState.collectAsStateWithLifecycle()
    val savedLocations by savedPlacesViewModel.savedLocations.collectAsStateWithLifecycle()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }

    LaunchedEffect(savedLocations) {
        if (savedLocations.isNotEmpty()) {
            landingPageViewModel.loadDataForLocations(savedLocations)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hjem") })
        },
        bottomBar = {
            BottomNavBar(navController = navController, savedPlacesViewModel = savedPlacesViewModel)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (savedLocations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Ingen lagrede steder ennå.")
                }
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
                        val hasAlert = (uiState.locationAlerts[location.id] ?: emptyList()).isNotEmpty()
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
                val selectedLocation = savedLocations.getOrNull(selectedTabIndex) ?: savedLocations[0]
                val alertsForSelectedLocation = uiState.locationAlerts[selectedLocation.id] ?: emptyList()
                val forecastForSelectedLocation = uiState.locationForecasts[selectedLocation.id]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))


                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(text = "Værvarsel", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = if (forecastForSelectedLocation != null) {
                                "${forecastForSelectedLocation.description} ${forecastForSelectedLocation.temperature}°C"
                            } else {
                                "Henter vær..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )


                        if (alertsForSelectedLocation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            alertsForSelectedLocation.forEach { alert ->
                                LocationAlertBanner(alert)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TODO når bruker klikker på knappen så skal den vise dagens råd fra AI, Haris bør fikse det :)
                    Button(
                        onClick = { /* Naviger til AI-råd */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Se dagens råd fra AI", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    //Vanning Seksjon
                    Text(
                        text = "Vanning",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // TODO: Her legges PlantCard-loopen din senere
                }
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