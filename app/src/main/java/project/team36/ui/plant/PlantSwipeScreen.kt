package project.team36.ui.plant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.team36.ui.plant.PlantSwipeViewModel
import project.team36.ui.plant.PlantCard

@Composable
fun PlantSwipeScreen(
    viewModel: PlantSwipeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val plants by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // VIKTIG FIKS: Sjekker at listen faktisk har innhold, ikke bare at den ikke er null!
        if (plants.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selve kortet
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PlantCard(plant = plants[0])
                }

                // Handlingsknapper under kortet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hopp over-knapp (kaller dislike i viewmodel for å fjerne fra toppen)
                    OutlinedButton(
                        onClick = { viewModel.onDislike() },
                        modifier = Modifier.height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Hopp over")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hopp over")
                    }

                    // Legg til-knapp (kaller like i viewmodel)
                    Button(
                        onClick = { viewModel.onLike() },
                        modifier = Modifier.height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Mørk grønn
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Legg til")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Legg til")
                    }
                }
            }
        } else {
            // Denne teksten vises hvis CSV-filen er lest til slutten, eller hvis det var en feil i CSV-filen
            Text(
                text = "Ingen flere planter å vise!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )
        }
    }
}
