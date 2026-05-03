package project.team36.ui.landingpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import kotlin.collections.isNotEmpty


@Composable
fun DailyTasksCard (
    plantsByLocation: Map<SavedLocation, List<Pair<SavedPlant, String>>>,
    onMarkAsWatered: (SavedPlant) -> Unit,
    onNavigateToPlant: (SavedPlant) -> Unit,
) {
    val hasAnyPlants = plantsByLocation.values.any { it.isNotEmpty() }

    Column {
        Text(
            text = "Dagens oppgaver",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
            color = Color(0xFF316322)
        )


        Card(
            modifier = Modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF316322))
        ) {

            if (!hasAnyPlants) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Ingen planter trenger vann i dag!",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF316322)
                    )
                }
            } else {

                plantsByLocation.forEach { (location, plantsWithText) ->
                    if (plantsWithText.isNotEmpty()) {
                        Text(
                            text = location.name ?: "Ukjent hage",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                plantsWithText.forEach { (plant, wateringText) ->
                                    WateringPlantCard(
                                        plant = plant,
                                        wateringText = wateringText,
                                        onMarkAsWatered = { onMarkAsWatered(plant) },
                                        onNavigateToPlant = { onNavigateToPlant(plant) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WateringPlantCard(
    plant: SavedPlant,
    wateringText: String,
    onMarkAsWatered: () -> Unit,
    onNavigateToPlant: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = imageId),
                contentDescription = plant.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = plant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            //"må vannes" chip, hardkodet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFD32F2F), RoundedCornerShape(50)) //TODO; ENDRE TILBAKE TIL ColorAlert
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.wateringcan),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Må vannes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }


        }

        //innhold når kortet er expanded
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8E3))
                ) {

                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Vanningsbehov",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            fontSize = 13.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = wateringText,
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = onMarkAsWatered,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5DBFF)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Jeg har vannet", color = Color.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToPlant,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F8E3)) //TODO; ENDRE TILBAKE TIL ColorBackground
                ) {
                    Text("Gå til plante",color = Color.DarkGray, fontSize = 13.sp)
                }
            }
        }

        //pil for å expande kortet
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore,
                    contentDescription = "Utvid"
                )
            }
        }
    }
}