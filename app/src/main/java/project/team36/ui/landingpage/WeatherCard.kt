package project.team36.ui.landingpage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

//værkort som viser temperatur, vær ikon, vær beskrivelse, nedbør
@Composable
fun WeatherCard(
    weatherUiState: WeatherUiState?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            weatherUiState != null -> {
                Row(
                    modifier     = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Værbeskrivelse
                        Text(
                            text = weatherUiState.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        //Vær ikonene blir henta direkte fra assets med Coil. API'et gir allerede navn for været i string, slipper mapping når man bruker assets
                        AsyncImage(
                            model = "file:///android_asset/${weatherUiState.iconName}.svg",
                            contentDescription = weatherUiState.description,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))

                    //Tekst som viser temperatur og nedbær
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = weatherUiState.temperature,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = weatherUiState.precipitation,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            //Hvis man ikke er koblet til internett eller hvis API-kall til MET ikke fungerer
            else -> {
                Text(
                    text = "Kunne ikke hente vær",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}