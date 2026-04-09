package project.team36.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavHostController
import project.team36.model.location.SavedLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoCard(
    location: SavedLocation,
    // NYE PARAMETERE FOR AI:
    onGetAiAdvice: (Double, Double) -> Unit,
    aiResponse: String?,
    isAiLoading: Boolean,
    onDismissAi: () -> Unit,
    // EKSISTERENDE PARAMETERE:
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    navController: NavHostController //DENNE GIR ERROR; IGNORER!
) {
    var showEditDialog by rememberSaveable { mutableStateOf((false)) }
    var editedName by rememberSaveable { mutableStateOf(location.name ?: "")}

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

    // NY BOKS: Dette er popupen som viser rådet fra Claude når den er ferdig
    if (aiResponse != null) {
        AlertDialog(
            onDismissRequest = onDismissAi,
            title = { Text("Hageråd fra Claude 🌱") },
            text = { Text(aiResponse, modifier = Modifier .verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = onDismissAi) { Text("OK") }
            }
        )
    }

    //dette viser selve lokasjon og litt info
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(316.dp)
                .height(290.dp) // ØKT HØYDE FRA 230 TIL 290 FOR Å FÅ PLASS TIL KNAPPEN
                .background(color = Color(0xFFE8F8E3), shape = RoundedCornerShape(size = 15.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //viser custom navn, adresse hvis brukeren ikke har redigert navnet
                Text(text = location.name ?: "Ukjent", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                //viser adressen
                Text(
                    text = location.address ?: "Ingen adresse",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF6F8069)
                )


                //flytter den "lukk" knappen helt nederst
                Spacer(modifier = Modifier.weight(1f))


                //TODO: Fikse navigasjon til "Mine planter" screen
                //"Se mine planter" knapp
                Button(
                    onClick = { navController.navigate("myPlants")}, //KOMMER FEIL, IGNORER DEN
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

                // NY AI-KNAPP
                Button(
                    onClick = {
                        // Henter koordinatene for denne spesifikke plassen og sender til viewmodel
                        onGetAiAdvice(location.lat ?: 0.0, location.lon ?: 0.0)
                    },
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
                    enabled = !isAiLoading, // Låser knappen mens vi venter på svar
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Mørkere grønnfarge
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "✨ Få AI-Råd",
                            fontSize = 15.sp,
                            fontWeight = FontWeight(400)
                        )
                    }
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