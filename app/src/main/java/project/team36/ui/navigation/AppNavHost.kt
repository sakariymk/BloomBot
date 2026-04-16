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
    plantRepository: PlantRepository,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantDetailsViewModel: PlantDetailsViewModel, // SJEKK OM DENNE SKAL VÆRE HER
    landingPageViewModel: LandingPageViewModel,
) {

    val myPlantsViewModel: MyPlantsViewModel = viewModel()

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
                myPlantsViewModel = myPlantsViewModel
            )
        }
        composable ("findPlants/{locationName}") { backStackEntry ->
            val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
            FindPlantsScreen(
                findPlantsViewModel = findPlantsViewModel,
                myPlantsViewModel = myPlantsViewModel,
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
            )
        }
        // plantDetailScreen
        composable ("plantDetailsScreen/{plantName}/{action}") { backStackEntry ->
            val plantName = backStackEntry.arguments?.getString("plantName") ?: return@composable
            val action = backStackEntry.arguments?.getString("action") ?: ""
            val savedLocations = savedPlacesViewModel.savedLocations.collectAsState()
            val pendingPlant = myPlantsViewModel.pendingPlant.collectAsState()
            PlantDetailsScreen(
                plantName = plantName,
                viewModel = plantDetailsViewModel,
                onBackClick = { navController.popBackStack() },
                onAddPlant = if (action == "add") {
                    { plantDetailsViewModel.plant.value?.let { myPlantsViewModel.requestAddPlant(it) } }
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
                        myPlantsViewModel.confirmAddPlant(plant, location)
                        savedPlacesViewModel.selectLocation(location)
                        navController.navigate("myPlants")
                                },
                    onDismiss = { myPlantsViewModel.dismissAddPlant() }
                )
            }
        }
    }
}