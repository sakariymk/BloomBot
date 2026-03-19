package project.team36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// --- DINE EGNE FILER MÅ IMPORTERES HER ---
import project.team36.data.plante.PlantDataSource
import project.team36.data.plante.PlantRepository
import project.team36.ui.map.MapScreen
import project.team36.ui.map.MapViewModel
import project.team36.ui.plante.PlantSwipeScreen
import project.team36.ui.plante.PlantSwipeViewModel // <--- DENNE FIKSER DE TO ØVERSTE FEILENE
import project.team36.ui.theme.Team36Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSource = PlantDataSource(this)
        val repository = PlantRepository(dataSource)

        setContent {
            Team36Theme {
                var currentScreen by remember { mutableStateOf("map") }

                val swipeViewModel = remember { PlantSwipeViewModel(repository) }
                val mapViewModel: MapViewModel = viewModel()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == "map",
                                onClick = { currentScreen = "map" },
                                label = { Text("Kart") },
                                icon = { Icon(Icons.Default.Place, contentDescription = "Kart") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "swipe",
                                onClick = { currentScreen = "swipe" },
                                label = { Text("Finn Planter") },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Sveip") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "map" -> {
                                MapScreen(
                                    modifier = Modifier,
                                    viewModel = mapViewModel
                                )
                            }
                            "swipe" -> {
                                PlantSwipeScreen(
                                    viewModel = swipeViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}