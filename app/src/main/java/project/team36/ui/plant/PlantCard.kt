package project.team36.ui.plant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.tooling.preview.Preview
import project.team36.R
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType

@Composable
fun PlantCard(
    plant: PlantInfo,
    showPlantStatus: Boolean = false, //kort som vises på MinePlanter
    showAddButton: Boolean = false, //Kort som vises på VelgPlanter
    onAddPlant: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column (
            modifier = Modifier.fillMaxWidth(),
        ) {
            val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

            // Lite kort
            AnimatedVisibility(visible = !expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "Bilde av ${plant.name}",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = plant.name,
                        modifier = Modifier.weight(1f)
                        )
                    Text(
                        text = if (plant.daysSinceWatered < plant.maxDaysWithoutWater) "Status: Trenger vann" else "Status: Vannet",
                        color = if(plant.daysSinceWatered < plant.maxDaysWithoutWater) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            //Utvidet kort
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "Bilde av ${plant.name}",
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(100.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
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
                    Text(text = "Vannbehov: ${plant.waterPerWeek} ganger per uke", style = MaterialTheme.typography.bodyMedium)

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
                    if (showPlantStatus) {
                        //legg til logikk for å sjekke vanning
                    }

                    if (showAddButton){
                        Button(
                            onClick = { onAddPlant?.invoke() },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) { Text("Legg til") }
                    }
                }


            }
            // Knapp for utvidelse eller lukking
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Lukk" else "Åpne"
                )
            }

        }
    }
}


@Preview
@Composable
fun PlantCardPreview() {
    MaterialTheme {
        PlantCard(
            plant = PlantInfo(
                name = "Tulipan",
                soil = setOf(SoilType.LOAM, SoilType.CLAY),
                light = setOf(LightType.FULL),
                isFrostResistant = false,
                waterPerWeek = 2.0f,
                plantingMonth = setOf(4, 5),
                climateZone = 3,
                hasBeenWatered = false,
                daysSinceWatered = 0,
                maxDaysWithoutWater = 7,
                imageRes = R.drawable.defaultplant,
                description = "En klassisk hageplante med vakre blomster."
            )
        )
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