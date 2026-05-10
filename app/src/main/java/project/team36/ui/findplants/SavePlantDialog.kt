package project.team36.ui.findplants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.team36.model.location.SavedLocation
import project.team36.ui.plant.PlantDetails

@Composable
fun SavePlantDialog(
    plant: PlantDetails,
    savedLocations: List<SavedLocation>,
    onConfirm: (SavedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<SavedLocation?>(null) } //endret fra rememberSaveable, grunnen til kræsj av app ved testing

    AlertDialog(
        onDismiss,
        title = { Text("Hvor vil du lagre ${plant.name.replaceFirstChar { it.lowercase() }}?") },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                if (savedLocations.isEmpty()) {
                    Text("Du har ingen lagrede hager enda. Legg til en hage på kartet først.")
                }
                else {
                    savedLocations.forEach { location ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLocation == location,
                                onClick = {selectedLocation = location}
                            )
                            Text(
                                text = location.name?: "Ukjent",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                ),
                onClick = { selectedLocation?.let { onConfirm(it) } },
                enabled = selectedLocation != null
            ) { Text("Legg til") }
        },
        dismissButton = {
            OutlinedButton(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                onClick = onDismiss
            ) {Text("Avbryt")
            }
        }
    )
}