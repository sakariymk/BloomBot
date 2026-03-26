import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import project.team36.data.plant.PlantDataSource
import project.team36.data.plant.PlantRepository
import project.team36.ui.findplants.FindPlantsScreen
import project.team36.ui.findplants.FindPlantsViewModel
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.map.SavedPlacesViewModel
import project.team36.ui.myplants.MyPlantsScreen
import project.team36.ui.myplants.MyPlantsViewModel
import project.team36.ui.plant.PlantSwipeScreen
import project.team36.ui.plant.PlantSwipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    findPlantsViewModel: FindPlantsViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    swipeViewModel: PlantSwipeViewModel,
    myPlantsViewModel: MyPlantsViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val plantDataSource = PlantDataSource(context)
    val plantRepository = PlantRepository(plantDataSource)

    NavHost(
        navController = navController,
        startDestination = "map",
        modifier = modifier
    ) {
        composable("map") {
            MapScreen(
                modifier = modifier,
                viewModel = mapViewModel,
                navController = navController,
                savedPlacesViewModel = savedPlacesViewModel
            )
        }
        composable("myPlants") {
            MyPlantsScreen(
                navController = navController,
                savedPlacesViewModel = savedPlacesViewModel,
                plantRepository = plantRepository,
                myPlantsViewModel = myPlantsViewModel
            )
        }
        composable("findPlants") {
            FindPlantsScreen(
                findPlantsViewModel = findPlantsViewModel,
                navController = navController,
                myPlantsViewModel = myPlantsViewModel
            )
        }
        composable("swipe") {
            PlantSwipeScreen(
                viewModel = swipeViewModel,
                navController = navController
            )
        }
    }
}