package project.team36.ui.findplants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.team36.ui.myplants.MyPlantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPlantsScreen (
    viewModel: FindPlantsViewModel = viewModel(),
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oppdag") }
            )
        },
        bottomBar = {
            NavigationBar{
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("map")},
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Kart")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("myPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Mine planter")}
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("findPlants") },
                    icon = { }, //fyller ut med icon senere!
                    label = { Text("Oppdag planter")}
                )
            }
        }
    ) {
        innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //TODO
        }
    }
}