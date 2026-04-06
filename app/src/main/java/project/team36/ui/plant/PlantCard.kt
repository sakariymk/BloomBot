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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.style.TextAlign
import project.team36.model.plant.LightType

@Composable
fun PlantCard(
    plante: PlantInfo,
    modifier: Modifier = Modifier
) {
    val sunScore = if (LightType.FULL in plante.light) 10 else if (LightType.PARTIAL in plante.light) 6 else 2

    val waterScore = (plante.waterPerWeek * 2).toInt().coerceIn(1, 10)

    val hardinessScore = plante.climateZone.coerceIn(1, 10)

    val frostScore = if (plante.isFrostResistant) 10 else 2

    val overallScore = (sunScore + waterScore + hardinessScore + frostScore) / 4

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(4.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (plante.imageRes != 0) {
                    Image(
                        painter = painterResource(id = plante.imageRes),
                        contentDescription = plante.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Info, contentDescription = "Bilde mangler", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plante.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(thickness = 4.dp, color = Color.Black)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = plante.description,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            SegmentedProgressBar(label = "Solbehov", value = sunScore)
            SegmentedProgressBar(label = "Vannmengde", value = waterScore)
            SegmentedProgressBar(label = "Hardførhet (Sone)", value = hardinessScore)
            SegmentedProgressBar(label = "Frosttålelig", value = frostScore)

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Totalvurdering:", fontSize = 16.sp)
            Text(
                text = "$overallScore/10",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
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