package project.team36.ui.plante

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
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
 * BESKRIVELSE: Viser bunken med plantekort og knapper for å navigere.
 */

@Composable
fun PlantSwipeScreen(
    viewModel: PlantSwipeViewModel,
    modifier: Modifier = Modifier
) {
    // Observerer listen med planter fra ViewModel
    val plants by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (plants.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Viser den øverste planten i lista
                Box(modifier = Modifier.weight(1f)) {
                    PlantCard(plante = plants[0])
                }

                // Knapper for å etterligne Tinder-sveip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // NEI-knapp
                    LargeFloatingActionButton(
                        onClick = { viewModel.onDislike() },
                        containerColor = Color(0xFFFFCDD2) // Lys rød
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Nei", tint = Color.Red)
                    }

                    // JA-knapp
                    LargeFloatingActionButton(
                        onClick = { viewModel.onLike() },
                        containerColor = Color(0xFFC8E6C9) // Lys grønn
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Liker", tint = Color(0xFF2E7D32))
                    }
                }
            }
        } else {
            // Vises når bunken er tom
            Text(
                text = "Ingen flere planter i nærheten!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )
        }
    }
}