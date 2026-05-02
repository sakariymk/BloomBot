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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.data.mcp.WeatherAlert
import project.team36.model.location.SavedPlant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Farger for kortet - se om de kan lagres i Theme


@Composable
fun PlantCard(
    plant: SavedPlant,
    needsWater: Boolean,
    daysUntilWatering: Long,
    daysSinceFertilizing: Long,
    onMarkAsWatered: () -> Unit,
    onMarkAsFertilized: () -> Unit,
    onNavigateToDetails: () -> Unit, //Endret fra navController, så ikke composable vet noe om navigasjon :)
    //alerts: List<WeatherAlert>
) {
    var expanded by remember { mutableStateOf(false) }

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
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                //Chip for vannestatus
                if (needsWater) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.wateringcan),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Må vannes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            // Utvidet kort
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
                        subtitle = when (daysUntilWatering) {
                            0L -> "Må vannes nå!"
                            1L -> "$daysUntilWatering dag til neste vanning"
                            else -> "$daysUntilWatering dager til neste vanning"
                        },
                        buttonText = "Jeg har vannet",
                        buttonColor = MaterialTheme.colorScheme.onPrimary,
                        onButtonClick = onMarkAsWatered
                    )


//                    // Farevarsler (Værvarsel)
//                    WeatherSection(alerts = alerts, plant = plant)

                    //Gjødsling av planten, oppdaterer dato for sist gjødsel gitt for sporing
                    InfoSection(
                        title = "Gjødsel",
                        subtitle = if (plant.lastFertilizationDate == 0L) {
                            "Ikke gjødslet enda"
                        } else {
                            "$daysSinceFertilizing dager siden sist gjødsling (${plant.lastFertilizationDate.toDisplayDate()})"
                        },
                        buttonText = "Jeg har gjødslet",
                        buttonColor = MaterialTheme.colorScheme.onPrimary,
                        onButtonClick = onMarkAsFertilized
                    )
                    // Knapp for å navigere til detaljskjerm for plante med en slett-knapp
                    Button(
                        onClick = onNavigateToDetails,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Se detaljer",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
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
private fun StatusChip(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.wateringcan),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
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
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,

            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(buttonColor),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text(
                text = buttonText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
//Hjelpefunksjon for å definere displayformatet på dato fra database
fun Long.toDisplayDate(): String {
    if (this == 0L) return "Ikke gjødslet enda"
    val sdf = SimpleDateFormat("dd.MMMM yyyy", Locale.forLanguageTag("no"))
    return sdf.format(Date(this))
}

@Composable
private fun WeatherSection(
    alerts: List<WeatherAlert>,
    plant: SavedPlant
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Værvarsel",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (alerts.isEmpty()) {
            Text(
                text = "Ingen dårlig vær i sikte for dine planter.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        } else {
            alerts.forEach { alert ->
                PlantAlertInfo(alert = alert, plant = plant)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PlantAlertInfo(alert: WeatherAlert, plant: SavedPlant) {
    val isDangerous = alert.isDangerousForPlant(plant.isFrostResistant)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(alert.color.copy(alpha = 0.2f))
            .border(1.dp, alert.color, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isDangerous) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = alert.color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = alert.event.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color.Black
            )
        }

        Text(
            text = alert.description,
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp),
            lineHeight = 16.sp
        )

        Text(
            text = if (isDangerous) "Kritisk for denne planten!" else "Planten bør tåle dette",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isDangerous) Color(0xFFD32F2F) else Color(0xFF2E7D32),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/*
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
*/
