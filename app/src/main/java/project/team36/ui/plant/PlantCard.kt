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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Farger for kortet - se om de kan lagres i Theme
private val ColorAlert = Color(0xFFFF9EA0) //varslingstag
private val ColorButtons = Color(0xFFB5DBFF) //knapper
private val ColorBackground = Color(0xFFE8F8E3) //bakgrunn på seksjoner

@Composable
fun PlantCard(
    plant: SavedPlant,
    needsWater: Boolean,
    daysUntilWatering: Long,
    daysSinceFertilizing: Long,
    onMarkAsWatered: () -> Unit,
    onMarkAsFertilized: () -> Unit,
    onNavigateToDetails: () -> Unit, //Endret fra navController, så ikke composable vet noe om navigasjon :)
) {
    var expanded by remember { mutableStateOf(true) }

    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Header som alltid er synlig
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Bilde av ${plant.name}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                //Chip for vannestatus
                if (needsWater) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = ColorAlert ,
                                RoundedCornerShape(50)
                            )
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
            }
            //Utvidet kort
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //Teller ned på vanningsdager siden sist vanning, eller siden det sist regnet 5 mm
                    InfoSection(
                        title = "Vanningsbehov",
                        subtitle = when {
                            daysUntilWatering == 0L -> "Må vannes nå!"
                            daysUntilWatering == 1L -> "$daysUntilWatering dag til neste vanning"
                            else -> "$daysUntilWatering dager til neste vanning"
                        },
                        buttonText = "Jeg har vannet",
                        buttonColor = ColorButtons,
                        onButtonClick = onMarkAsWatered
                    )
                    //Gjødsling av planten, oppdaterer dato for sist gjødsel gitt for sporing
                    InfoSection(
                        title = "Gjødsel",
                        subtitle = if (plant.lastFertilizationDate == 0L) {
                            "Ikke gjødslet enda"
                        } else {
                            "$daysSinceFertilizing dager siden sist gjødsling (${plant.lastFertilizationDate.toDisplayDate()})"
                        },
                        buttonText = "Jeg har gjødslet",
                        buttonColor = ColorButtons,
                        onButtonClick = onMarkAsFertilized
                    )
                    // Knapp for å navigere til detaljskjerm for plante med en slett-knapp
                    Button(
                        onClick = onNavigateToDetails,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorBackground)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Se detaljer",
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            // Knapp for utvidelse eller lukking av boksen
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

@Composable
private fun InfoSection(
    title: String,
    subtitle: String,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray,

            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text(
                text = buttonText,
                fontSize = 13.sp,
                color = Color.Black
            )
        }
    }
}

//Hjelpefunksjon for å definere displayformatet på dato fra database
fun Long.toDisplayDate(): String {
    if (this == 0L) return "Ikke gjødslet enda"
    val sdf = SimpleDateFormat("dd.MMMM yyyy", Locale("no"))
    return sdf.format(Date(this))
}

@Preview(showBackground = true)
@Composable
fun PlantCardPreview() {
    val fakePlant = SavedPlant(
        id = 1,
        locationId = 1,
        name = "Pelargonia",
        soil = setOf(SoilType.LOAM),
        light = setOf(LightType.FULL),
        isFrostResistant = false,
        waterPerWeek = 3f,
        plantingMonth = setOf(4, 5),
        climateZone = 3,
        hasBeenWatered = false,
        daysSinceWatered = 2,
        maxDaysWithoutWater = 3,
        imageRes = 0,
        description = "En vakker plante.",
        lastWateredDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L),
        lastFertilizationDate = System.currentTimeMillis()
    )

    MaterialTheme {
        PlantCard(
            plant = fakePlant,
            needsWater = true,
            daysUntilWatering = 1L,
            daysSinceFertilizing = 0L,
            onMarkAsWatered = {},
            onMarkAsFertilized = {},
            onNavigateToDetails = {}
        )
    }
}
