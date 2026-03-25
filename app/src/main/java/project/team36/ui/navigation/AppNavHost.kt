package project.team36.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.myplants.MyPlantsViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val mapViewModel: MapViewModel = viewModel()
    val myPlantsViewModel: MyPlantsViewModel = viewModel()
    val findPlantsViewModel: FindPlantsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "map",
        modifier = modifier
    ){
        composable("map") {
            MapScreen(
                modifier,
                viewModel = mapViewModel,
                navController = navController
            ) }
        composable ("myPlants"){
            MyPlantsScreen(
                viewModel = myPlantsViewModel,
                mapViewModel = mapViewModel,
                navController = navController
            ) }
        composable ("findPlants") {
            FindPlantsScreen (
                viewModel = findPlantsViewModel,
                navController = navController
            )
        }
    }
}