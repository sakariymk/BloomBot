package project.team36.ui.findplants

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.team36.model.location.SavedLocation

@Composable
fun SwitchLocationDialog (
    savedLocations: List<SavedLocation>,
    currentLocationName: String,
    onConfirm: (SavedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember() { mutableStateOf(savedLocations.find { it.name == currentLocationName }) }

    AlertDialog (
        onDismissRequest = onDismiss,
        title = { Text("Endre hage") },
        text = {
            Column {
                if (savedLocations.isEmpty()) {
                    Text("Du har ingen lagrede hager enda. Legg til en ny hage.") // kanskje ordne CTA til kartet her?
                } else {
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
                onClick = {selectedLocation?.let { onConfirm(it) } },
                enabled = selectedLocation != null,
            ) { Text("Bekreft") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Avbryt") }
        }
    )
}