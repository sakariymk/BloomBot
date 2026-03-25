package project.team36.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PlaceInfoCard(
    place: SavedPlace,
    isFetchingRecommendation: Boolean,
    onDismiss: () -> Unit,
    onGetRecommendationClicked: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(316.dp)
                .wrapContentHeight()
                .background(color = Color(0xFFE8F8E3), shape = RoundedCornerShape(size = 15.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = place.customName, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                if (place.customName != place.result.name) {
                    Text(text = place.result.name, color = Color.Gray, fontSize = 14.sp)
                }

                Text(
                    text = place.result.fullAddress ?: place.result.address?.region ?: "Ingen adresse",
                    modifier = Modifier.padding(top = 4.dp)
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Lukk")
                }

                Button(
                    onClick = onGetRecommendationClicked,
                    enabled = !isFetchingRecommendation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    if (isFetchingRecommendation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Få AI Planteanbefaling",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}