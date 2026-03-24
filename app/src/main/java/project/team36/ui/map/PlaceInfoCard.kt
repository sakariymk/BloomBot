

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import project.team36.ui.map.SavedPlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoCard(
    place: SavedPlace,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf((false)) }
    var editedName by rememberSaveable { mutableStateOf(place.customName)}

    //dette er boks hvis bruker vil redigere stedet man har lagra
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Rediger lokasjon") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Endre navn") }
                )
            },
            confirmButton = {
                Button(onClick = {onEdit(editedName); showEditDialog = false}) {
                    Text("Lagre")
                }
            },
            dismissButton = {
                TextButton(onClick = {onDelete(); showEditDialog = false}) {
                    Text("Slett lokasjon", color = Color.Red)
                }
            }

        )
    }


    //dette viser selve lokasjon og litt info
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(316.dp)
                .height(230.dp)
                .background(color = Color(0xFFE8F8E3), shape = RoundedCornerShape(size = 15.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //viser custom navn, adresse hvis brukeren ikke har redigert navnet
                Text(text = place.customName, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                //viser adressen
                Text(
                    text = place.result.fullAddress ?: place.result.address?.region ?: "Ingen adresse",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF6F8069)
                )


                //flytter den "lukk" knappen helt nederst
                Spacer(modifier = Modifier.weight(1f))


                //TODO: Fikse navigasjon til "Mine planter" screen
                //"Se mine planter" knapp
                Button(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(6.dp)
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
                        text = "🌱Se mine planter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }


                //"Rediger lokasjon" knapp
                Button(
                    onClick = {showEditDialog = true}, //knappen gjør at edit dialog kommer opp
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(6.dp)
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
                        text = "⚙️Rediger lokasjon",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }

                //knapp for å lukke boksen
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text("Lukk", color = Color(0xFF6F8069))
                }
            }
        }
    }
}

