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
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.TopAppBar
    import androidx.compose.material3.TopAppBarDefaults
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import androidx.navigation.NavController
    import com.google.accompanist.permissions.ExperimentalPermissionsApi
    import com.google.accompanist.permissions.rememberPermissionState
    import project.team36.ui.map.SavedPlacesViewModel
    import project.team36.ui.myplants.MyPlantsViewModel
    import project.team36.ui.navigation.BottomNavBar

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
        val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val allPlants by myPlantsViewModel.allPlants.collectAsStateWithLifecycle()

        val plantsByLocation = remember(savedLocations, allPlants) {
            savedLocations.associateWith { location ->
                allPlants
                    .filter { plant ->
                        plant.locationId == location.id && myPlantsViewModel.needsWater(plant)
                    }
                    .map { plant ->
                        plant to myPlantsViewModel.getWateringText(plant)
                    }
            }
        }

        LaunchedEffect(Unit) {
            locationPermission.launchPermissionRequest()
        }

        LaunchedEffect(savedLocations) {
            if (savedLocations.isNotEmpty()) {
                landingPageViewModel.loadWeatherForLocations(savedLocations)
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE8F5E2)
                    )
                )
            },

            bottomBar = { BottomNavBar(navController = navController, savedPlacesViewModel) }

        ) { innerPadding ->

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                if (savedLocations.isEmpty()) {
                    Text(
                        text = "Ingen lagrede steder ennå.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
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
                            }
                        }
                    }
                }

                DailyTasksCard(
                    plantsByLocation = plantsByLocation,
                    onMarkAsWatered = {myPlantsViewModel.markAsWatered(it)},
                    navController = navController,
                )
            }
        }
    }