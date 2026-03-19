package project.team36.ui.plante

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.team36.model.plante.PlanteInfo

/**
 * FIL: PlantCard.kt
 * TYPE: VANLIG FIL (Composable)
 * BESKRIVELSE: Viser informasjonen om én spesifikk plante.
 */

@Composable
fun PlantCard(
    plante: PlanteInfo,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            // Navn på planten
            Text(
                text = plante.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Liste over teknisk info fra CSV
            PlantInfoRow("Sone", "${plante.minZone} til ${plante.maxZone}")
            PlantInfoRow("Vanning", "${plante.waterPerWeek} L per uke")
            PlantInfoRow("Temperatur", "Tåler ned til ${plante.minTemp}°C")
            PlantInfoRow("Lysforhold", plante.light.joinToString(", ") { it.name.lowercase() })
            PlantInfoRow("Jordtype", plante.soil.joinToString(", ") { it.name.lowercase() })
            PlantInfoRow("Fuktighet", "Nivå ${plante.humidityLevel}/10")
            PlantInfoRow("Sesong", plante.plantingSeason.name.lowercase())

            Spacer(modifier = Modifier.weight(1f))

            // Frost-indikator nederst
            Surface(
                color = if (plante.isFrostResistant) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    text = if (plante.isFrostResistant) "✓ Frosttolerant" else "✕ Sårbar for frost",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun PlantInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}