package project.team36.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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

//import project.team36.ui.plant.PlantSwipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantDetailsViewModel: PlantDetailsViewModel // SJEKK OM DENNE SKAL VÆRE HER
) {

    val myPlantsViewModel: MyPlantsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "map",
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
                mapViewModel = mapViewModel,
                navController = navController,
                savedPlacesViewModel = savedPlacesViewModel,
                myPlantsViewModel = myPlantsViewModel
            )
        }
        composable ("findPlants") {
            FindPlantsScreen(
                findPlantsViewModel = findPlantsViewModel,
                myPlantsViewModel = myPlantsViewModel,
                navController = navController
            )
        }
        // plantDetailScreen
        composable ("plantDetail/{plantName}") { backStackEntry ->
            val plantName = backStackEntry.arguments?.getString("plantName") ?: return@composable
            PlantDetailsScreen(
                plantName = plantName,
                viewModel = plantDetailsViewModel,
                onBackClick = { navController.popBackStack() },
                onAddClick = {/*TODO*/}
            )
        }
    }
}