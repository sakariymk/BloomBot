package project.team36.ui.landingpage

import android.Manifest
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.navigation.BottomNavBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp

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

    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }

    LaunchedEffect(savedLocations) {
        if (savedLocations.isNotEmpty()) {
            landingPageViewModel.loadWeatherForLocations(savedLocations)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hjem") })
        },

        bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel) }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (savedLocations.isEmpty()) {
                Text(
                    text = "Ingen lagrede steder ennå.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }  else {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            savedLocations.forEachIndexed { index, location ->
                Column(
                    modifier = Modifier
                        .padding(start = if (index == 0) 16.dp else 0.dp)
                        .width(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = location.name ?: "Ukjent sted",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    WeatherCard(
                        weatherUiState = uiState.locationForecasts[location.id],
                        isLoading = uiState.isWeatherLoading,
                    )

                    // <--- endring: AI-knapp rett under WeatherCard
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { landingPageViewModel.fetchAiAdvice(location) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        enabled = !uiState.isAiLoading && !uiState.isFutureAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isAiLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("✨ Dagens Råd", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { landingPageViewModel.fetchFutureAiAdvice(location) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        enabled = !uiState.isAiLoading && !uiState.isFutureAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isFutureAiLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("🔮 Fremtids-Råd", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

        // <-- ENDRING: Popup-boksen som viser resultatet
        if (uiState.aiResponse != null) {
            AlertDialog(
                onDismissRequest = { landingPageViewModel.dismissAiAdvice() },
                title = { Text("Hageråd fra Claude 🌱", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Væroppsummering
                        Column {
                            Text("Været:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text(uiState.aiResponse!!.weatherSummary)
                        }

                        // 2. Planteråd
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

                        // 3. Generelle oppgaver
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

            //TODO: LAG RESTEN AV LANDINGSSIDEN!
        }
    }
}