package project.team36.ui.plant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.team36.R
import project.team36.data.mcp.WeatherAlert
import project.team36.model.location.SavedPlant

// Farger for kortet
private val ColorAlert = Color(0xFFFF9EA0) // varslingstag
private val ColorButtons = Color(0xFFB5DBFF) // knapper
private val ColorBackground = Color(0xFFE8F8E3) // bakgrunn på seksjoner

@Composable
fun PlantCard(
    plant: SavedPlant,
    needsWater: Boolean,
    daysUntilWatering: Long,
    onMarkAsWatered: () -> Unit,
    alerts: List<WeatherAlert>,
    navController: NavController
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = plant.description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chip for vannestatus
                if (needsWater) {
                    StatusChip(text = "Må vannes", color = ColorAlert)
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
                    // Vanningsseksjon
                    InfoSection(
                        title = "Vanningsbehov",
                        subtitle = if (daysUntilWatering <= 0L) "Må vannes nå" else "$daysUntilWatering dager til neste vanning",
                        buttonText = "Jeg har vannet",
                        buttonColor = ColorButtons,
                        onButtonClick = onMarkAsWatered
                    )

                    // Farevarsler (Værvarsel)
                    WeatherSection(alerts = alerts, plant = plant)

                    // Gjødsel (valgfritt)
                    InfoSection(
                        title = "Gjødsel",
                        subtitle = "13 dager til gjødsling",
                        buttonText = "Gjødslet",
                        buttonColor = ColorButtons,
                        onButtonClick = { /* TODO */ }
                    )

                    Button(
                        onClick = { navController.navigate("plantDetailsScreen/${plant.name}/delete") },
                        modifier = Modifier.align(Alignment.Start),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorBackground)
                    ) {
                        Text(
                            text = "Se detaljer ➜",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
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
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
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
            tint = Color.White,
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
            .background(ColorBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
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
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WeatherSection(
    alerts: List<WeatherAlert>,
    plant: SavedPlant
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorBackground, RoundedCornerShape(12.dp))
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
        lastWateredDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
    )

    MaterialTheme {
        PlantCard(
            plant = fakePlant,
            needsWater = true,
            daysUntilWatering = 1L,
            onMarkAsWatered = {},
            navController = rememberNavController()
        )
    }
}
*/
