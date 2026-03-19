package project.team36.ui.plante

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

/**
 * FIL: PlantSwipeScreen.kt
 * TYPE: VANLIG FIL (Composable)
 */

@Composable
fun PlantSwipeScreen(
    viewModel: PlantSwipeViewModel,
    modifier: Modifier = Modifier
) {
    val plants by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (plants.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selve kortet
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PlantCard(plante = plants[0])
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
            Text(
                text = "Ingen flere planter å vise!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )
        }
    }
}