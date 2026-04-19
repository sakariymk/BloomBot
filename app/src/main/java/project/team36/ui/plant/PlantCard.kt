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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import project.team36.R
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType

// Farger for kortet - se om de kan lagres i Theme=
private val ColorAlert = Color(0xFFFF9EA0) //varslingstag
private val ColorButtons = Color(0xFFB5DBFF) //knapper
private val ColorBackground = Color(0xFFE8F8E3) //bakgrunn på seksjoner
@Composable
fun PlantCard(
    plant: SavedPlant,
    needsWater: Boolean,
    daysUntilWatering: Long,
    onMarkAsWatered: () -> Unit,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

    // HUSK å legge til "les mer" button
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

                //Chip for vannestatus
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (needsWater) ColorAlert else ColorButtons,
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
                        text = if (needsWater) "Må vannes" else "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
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
                    //Vanningsseksjon
                    InfoSection(
                        title = "Vanningsbehov",
                        subtitle = if (daysUntilWatering <= 0L) "Må vannes nå" else "$daysUntilWatering dager til neste vanning",
                        buttonText = "Jeg har vannet",
                        buttonColor = ColorButtons,
                        onButtonClick = onMarkAsWatered
                    )
                    //Gjødsel eventuelt
                    InfoSection(
                        title = "Gjødsel",
                        subtitle = "13 dager til gjødsling",
                        buttonText = "Jeg har gjødslet",
                        buttonColor = ColorButtons,
                        onButtonClick = { /* TODO */ } //finn ut om vi skal ha gjødsling?
                    )

                    //Værsvarsel som vi må revampe heftig når det er klart
                    WeatherSection()

                    Button(
                        onClick = { navController.navigate("plantDetailsScreen/${plant.name}/delete") },
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 8.dp),
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
                fontSize = 13.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun WeatherSection(
    alerts: List<String> = emptyList() //TODO: bytt til riktig list når Sakariye er klar med parsing
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background((ColorBackground), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = "Værvarsel",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        //TODO: må byttes ut når vi kan hente farevarsler
        Text(
            text = "Ingen dårlig vær i sikte.",
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
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
