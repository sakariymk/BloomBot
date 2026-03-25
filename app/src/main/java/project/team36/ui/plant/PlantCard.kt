package project.team36.ui.plant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.team36.model.plant.PlantInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import project.team36.R

@Composable
fun PlantCard(plant: PlantInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.ic_launcher_background

            Image(
                painter = painterResource(id = imageId),
                contentDescription = "Bilde av ${plant.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (plant.isFrostResistant) {
                        Badge { Text("Tåler frost") }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Klimasone: H${plant.climateZone}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Vannbehov: ${plant.waterPerWeek}L/uke", style = MaterialTheme.typography.bodyMedium)

                Text(
                    text = if (plant.hasBeenWatered) "Status: Vannet" else "Status: Trenger vann",
                    color = if (plant.hasBeenWatered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Jordtyper:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(plant.soil.toList()) { soil ->
                        SuggestionChip(onClick = {}, label = { Text(soil.name) })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = plant.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun SegmentedProgressBar(label: String, value: Int, max: Int = 10) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 14.sp, modifier = Modifier.padding(bottom = 2.dp, start = 16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (i in 1..max) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (i <= value) Color(0xFFC0CA33) else Color.White)
                            .border(1.dp, Color.Black)
                    )
                }
            }

            Text("10", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        }
    }
}