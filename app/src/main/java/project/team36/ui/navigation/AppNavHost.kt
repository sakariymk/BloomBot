package project.team36.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import project.team36.data.plant.PlantRepository
import project.team36.ui.findplants.SavePlantDialog
import project.team36.ui.myplaces.MyPlacesScreen
import project.team36.ui.landingpage.LandingPageScreen
import project.team36.ui.landingpage.LandingPageViewModel

//import project.team36.ui.plant.PlantSwipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    myPlantsViewModel: MyPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantDetailsViewModel: PlantDetailsViewModel,
    landingPageViewModel: LandingPageViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = "landingpage", //HER ENDRER JEG BARE TIL LANDINGPAGE FOR Å SE DEN PÅ APPEN
        modifier = modifier
    ){
        composable("map") {
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
                landingPageViewModel = landingPageViewModel

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
        // plantDetailScreen
        composable ("plantDetailsScreen/{plantName}/{action}") { backStackEntry ->
            val plantName = backStackEntry.arguments?.getString("plantName") ?: return@composable
            val action = backStackEntry.arguments?.getString("action") ?: ""
            val savedLocations = savedPlacesViewModel.savedLocations.collectAsState()
            val pendingPlant = findPlantsViewModel.pendingPlant.collectAsState()
            PlantDetailsScreen(
                plantName = plantName,
                viewModel = plantDetailsViewModel,
                onBackClick = { navController.popBackStack() },
                onAddPlant = if (action == "add") {
                    { plantDetailsViewModel.plant.value?.let { findPlantsViewModel.requestAddPlant(it) } }
                } else null, //jeg (viktoria) endra litt på dette slik at appen kan buildes og kjøres
                onDeletePlant = if (action == "delete") {
                    { myPlantsViewModel.removePlantByName(plantName) }
                } else null//jeg (viktoria) endra litt på dette slik at appen kan buildes og kjøres
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
                onAddPlaceClick = { navController.navigate("map") }
            )
        }
    }
}