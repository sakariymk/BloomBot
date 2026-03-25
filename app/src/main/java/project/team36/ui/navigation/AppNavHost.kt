package project.team36.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.plant.PlantSwipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    swipeViewModel: PlantSwipeViewModel
) {
    //TODO - fiks viewmodels i alle screens også
    val mapViewModel: MapViewModel = viewModel()
    val findPlantsViewModel: FindPlantsViewModel = viewModel()
    val savedPlacesViewModel: SavedPlacesViewModel = viewModel()

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
                savedPlacesViewModel = savedPlacesViewModel
            )
        }
        composable ("findPlants") {
            FindPlantsScreen(
                viewModel = findPlantsViewModel,
                navController = navController
            )
        }
    }
}