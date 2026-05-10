package project.team36.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantDetailsScreen
import project.team36.ui.plant.PlantDetailsViewModel
import project.team36.ui.myplaces.MyPlacesScreen
import project.team36.ui.landingpage.LandingPageScreen
import project.team36.ui.landingpage.LandingPageViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import project.team36.ui.findplants.SavePlantDialog
import project.team36.ui.map.MapViewModelFactory

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    mapViewModelFactory: MapViewModelFactory,
    findPlantsViewModel: FindPlantsViewModel,
    myPlantsViewModel: MyPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantDetailsViewModel: PlantDetailsViewModel,
    landingPageViewModel: LandingPageViewModel,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(bottom = 105.dp)
                        .height(70.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = snackbarData.visuals.message)
                        IconButton(onClick = { snackbarData.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Lukk",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "landingpage",
            modifier = modifier.padding(innerPadding)
        ){
            composable("map") {
                val mapViewModel: MapViewModel = viewModel(factory = mapViewModelFactory)
                MapScreen(
                    modifier = modifier,
                    viewModel = mapViewModel,
                    navController = navController,
                    savedPlacesViewModel = savedPlacesViewModel
                )
            }
            composable ("myPlants"){
                MyPlantsScreen(
                    navController = navController,
                    savedPlacesViewModel = savedPlacesViewModel,
                    myPlantsViewModel = myPlantsViewModel,
                )
            }
            composable("findPlants/{locationName}") { backStackEntry ->
                val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
                FindPlantsScreen(
                    findPlantsViewModel = findPlantsViewModel,
                    navController = navController,
                    savedPlacesViewModel = savedPlacesViewModel,
                    locationName = locationName
                )
            }
            composable("landingpage") {
                LandingPageScreen(
                    navController = navController,
                    savedPlacesViewModel = savedPlacesViewModel,
                    landingPageViewModel = landingPageViewModel,
                    myPlantsViewModel = myPlantsViewModel
                )
            }
            composable ("plantDetailsScreen/{plantName}/{action}") { backStackEntry ->
                val plantName = backStackEntry.arguments?.getString("plantName") ?: return@composable
                val action = backStackEntry.arguments?.getString("action") ?: ""
                val savedLocations = savedPlacesViewModel.savedLocations.collectAsState()
                val pendingPlant = findPlantsViewModel.pendingPlant.collectAsState()

                LaunchedEffect(plantName) {
                    if (action == "add") {
                        plantDetailsViewModel.loadPlant(plantName)
                    } else {
                        plantDetailsViewModel.loadSavedPlant(plantName, myPlantsViewModel.plants.value)
                    }
                }

                PlantDetailsScreen(
                    navController = navController,
                    plantDetailsViewModel = plantDetailsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddPlant = if (action == "add") {
                        { plantDetailsViewModel.plant.value?.let { findPlantsViewModel.requestAddPlant(it) } }
                    } else null,
                    onDeletePlant = if (action == "delete") {
                        { myPlantsViewModel.removePlantByName(plantName) }
                    } else null
                )
                pendingPlant.value?.let { plant ->
                    SavePlantDialog(
                        plant = plant,
                        savedLocations = savedLocations.value,
                        onConfirm = { location ->
                            findPlantsViewModel.confirmAddPlant(plant, location)
                            savedPlacesViewModel.selectLocation(location)
                            navController.navigate("myPlants")
                        },
                        onDismiss = { findPlantsViewModel.dismissAddPlant() }
                    )
                }

            }
            composable("myPlaces") {
                MyPlacesScreen(
                    navController = navController,
                    savedPlacesViewModel = savedPlacesViewModel,
                    onLocationCardClick = { navController.navigate("myPlants") },
                    onAddPlaceClick = { navController.navigate("map") },
                )
            }
        }
    }
}