
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import project.team36.ui.map.SavedPlace

@Composable
fun PlaceInfoCard(
    place: SavedPlace,
    onDismiss: () -> Unit
) {
    //TODO: HER ER TING VELDIG STYGT OG ROTETE; EKSPERIMENTERTE MED STILER OG HVORDAN FÅ TIL TING MED MODIFIERS
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))

                .width(316.dp)
                .height(214.dp)
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
                    onClick = {},
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,  // bakgrunnsfarge
                        contentColor = Color.Black     // tekstfarge
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    Text(
                        text = "Se mine planter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }
            }
        }
    }
}