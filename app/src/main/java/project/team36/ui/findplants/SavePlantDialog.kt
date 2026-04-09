package project.team36.ui.findplants

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.team36.model.location.SavedLocation
import project.team36.model.plant.PlantInfo

@Composable
fun SavePlantDialog(
    plant: PlantInfo,
    savedLocations: List<SavedLocation>,
    onConfirm: (SavedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by rememberSaveable() { mutableStateOf<SavedLocation?>(null) }

    AlertDialog(
        onDismiss,
        title = { Text("Hvor vil du lagre ${plant.name}?") },
        text = {
            Column() {
                if (savedLocations.isEmpty()) {
                    Text("Du har ingen lagrede steder enda. Legg til et sted på kartet først")
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
            TextButton(
                onClick = { selectedLocation?.let { onConfirm(it) } },
                enabled = selectedLocation != null
            ) { Text("Legg til") }
        },
        dismissButton = { TextButton(onClick = onDismiss) {Text("Avbryt")}
        }
    )
}