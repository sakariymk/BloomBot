package project.team36.ui.plant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.team36.R
import project.team36.model.plant.PlantInfo

@Composable
fun PlantCard(
    plant: PlantInfo,
    showPlantStatus: Boolean = false, //kort som vises på MinePlanter
    showAddButton: Boolean = false, //Kort som vises på VelgPlanter
    onAddPlant: (() -> Unit)? = null,
    onDeletePlant: (() -> Unit)? = null,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    // HUSK å legge til "les mer" button
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(110.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = plant.name,
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = "Bilde av ${plant.name}",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 16.dp, bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = plant.name,
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
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
                    //feil med parsing av soiltype, men skal uansett endres i hele plantcard, så kommenterer ut for å få alt til å kjøre
//                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                        items(plant.soil.toList()) { soil ->
//                            SuggestionChip(onClick = {}, label = { Text(soil.name) })
//                        }
//                    }

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
                    } else {
                        Button(
                            onClick = { navController.navigate("plantDetailsScreen/${plant.name}/delete") },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) { Text("Se detaljer") }
                    }

                    // FJERN dette når PlantDetailsScreen er klart
//                    if (onDeletePlant != null) {
//                        Button(
//                            onClick = { onDeletePlant() },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Red,
//                                contentColor = Color.Black
//                            ),
//                            modifier = Modifier
//                                .align(Alignment.End)
//                                .padding(top = 8.dp)
//                        ) { Text("Slett") }
 //                   }
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
