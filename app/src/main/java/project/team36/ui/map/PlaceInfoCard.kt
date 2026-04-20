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
    // <------ ENDRING: Fikset at onGetAiAdvice krever SavedLocation, og la til onGetFutureAiAdvice parameter!
    onGetAiAdvice: (SavedLocation) -> Unit,
    onGetFutureAiAdvice: (SavedLocation) -> Unit,
    aiResponse: String?,
    isAiLoading: Boolean,
    isFutureAiLoading: Boolean,
    onDismissAi: () -> Unit,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    navController: NavHostController,


    // <------ ENDRING: Nye parametere for test-knappen
    onGetRawApiData: (SavedLocation) -> Unit,
    rawApiData: String?,
    onDismissRawApi: () -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf((false)) }
    var editedName by rememberSaveable { mutableStateOf(location.name ?: "")}

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

    // <------ ENDRING: Popup for RAW API data
    if (rawApiData != null) {
        AlertDialog(
            onDismissRequest = onDismissRawApi,
            title = { Text("RAW API Data 🛠") },
            text = { Text(rawApiData, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = onDismissRawApi) { Text("Lukk") }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(316.dp)
                // <------ ENDRING: Økte til 340 for å gi nok plass til begge AI knappene pent over hverandre
                .height(340.dp)
                .background(color = Color(0xFFE8F8E3), shape = RoundedCornerShape(size = 15.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = location.name ?: "Ukjent", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Text(
                    text = location.address ?: "Ingen adresse",
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF6F8069)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate("myPlants")},
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    Text(
                        text = "🌱Se mine planter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }

                Button(
                    onClick = {showEditDialog = true},
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    Text(
                        text = "⚙️Rediger lokasjon",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(400)
                    )
                }

                // AI-KNAPP 1: Vanlig
                Button(
                    onClick = {
                        // <------ ENDRING: Sender nå bare inn location!
                        onGetAiAdvice(location)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    enabled = !isAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
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

                // <------ ENDRING: NY TEST-KNAPP FOR RAW API
                Button(
                    onClick = {
                        onGetRawApiData(location)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    enabled = !isAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray, // Mørk grå for å vise at det er en dev-knapp
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "🛠 Test RAW API",
                            fontSize = 15.sp,
                            fontWeight = FontWeight(400)
                        )
                    }
                }

                // <------ ENDRING: NY AI-KNAPP 2 for FREMTIDIG RÅD
                Button(
                    onClick = {
                        onGetFutureAiAdvice(location)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(193.dp)
                        .height(42.dp),
                    enabled = !isFutureAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32), // Litt mørkere grønn for å skille dem
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(23.5.dp)
                ) {
                    if (isFutureAiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "🔮 Fremtids-Råd",
                            fontSize = 15.sp,
                            fontWeight = FontWeight(400)
                        )
                    }
                }

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