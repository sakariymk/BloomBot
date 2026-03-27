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
import project.team36.data.plant.PlantRepository

//import project.team36.ui.plant.PlantSwipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    plantRepository: PlantRepository
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
                navController = navController,
                savedPlacesViewModel = savedPlacesViewModel,
                plantRepository = plantRepository

            )
        }
        composable ("findPlants") {
            FindPlantsScreen(
                findPlantsViewModel = findPlantsViewModel,
                myPlantsViewModel = myPlantsViewModel,
                navController = navController

            )
        }
    }
}