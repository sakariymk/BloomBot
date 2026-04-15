package project.team36.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import project.team36.R

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val activeRoute = currentRoute?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF243B37)
    ) {
        NavigationBarItem(
            selected = activeRoute == "landingpage",
            onClick = {navController.navigate("landingpage")},
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute == "landingpage") R.drawable.home_icon_thick
                        else R.drawable.home_icon
                    ),
                    contentDescription = "Hjem"

                )
            },
            label = {Text("Hjem")}
        )

        NavigationBarItem(
            selected = activeRoute == "myPlants",
            onClick = {navController.navigate("myPlants")},
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute == "myPlants") R.drawable.plant_icon_thick
                        else R.drawable.plant_icon
                    ),
                    contentDescription = "Mine planter"

                )
            },
            label = {Text("Mine planter")}
        )

        NavigationBarItem(
            selected = activeRoute == "findPlants",
            onClick = {navController.navigate("findPlants")},
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (activeRoute == "findPlants") R.drawable.search_icon_thick
                        else R.drawable.search_icon
                    ),
                    contentDescription = "Oppdag planter"

                )
            },
            label = {Text("Oppdag planter")}
        )
    }
}