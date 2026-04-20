package project.team36.ui.plant

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.team36.R
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType
import project.team36.ui.theme.Team36Theme

// SimplePlantCard brukes som plantekort i findPlants

@Composable
fun SimplePlantCard (
    plant : PlantInfo,
    onNavigateToPlantDetails : (() -> Unit)? = null
) {
    val imageId = if (plant.imageRes != 0) plant.imageRes else R.drawable.defaultplant

    Card(
        onClick = { onNavigateToPlantDetails?.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription =  "Se mer",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimplePlantInfoCardPreview() {
    val fakePlant = PlantInfo(
        name = "Lilje",
        soil = setOf(SoilType.SAND),
        light = setOf(LightType.FULL),
        isFrostResistant = false,
        waterPerWeek = 2f,
        plantingMonth = setOf(6, 7, 8),
        climateZone = 2,
        hasBeenWatered = false,
        daysSinceWatered = 3,
        maxDaysWithoutWater = 12,
        imageRes = R.drawable.pelargonia,
        description = "En vakker lilje."
    )

    Team36Theme {
        SimplePlantCard(plant = fakePlant)
    }
}