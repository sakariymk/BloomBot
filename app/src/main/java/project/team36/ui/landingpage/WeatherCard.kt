package project.team36.ui.landingpage

import android.util.Log.i
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod
import project.team36.R
import project.team36.data.mcp.WeatherForecast

fun weatherSymbolToDescription(symbol: String): String {
    val base = symbol.substringBeforeLast("_")
    return when (base) {
        "clearsky"       -> "Klarvær"
        "fair"           -> "Lettskyet"
        "partlycloudy"   -> "Delvis skyet"
        "partlycloydy_day" -> "Delvis skyet"
        "cloudy"         -> "Overskyet"
        "fog"            -> "Tåke"
        "rain"           -> "Regn"
        "heavyrain"      -> "Kraftig regn"
        "lightrain"      -> "Lett regn"
        "snow"           -> "Snø"
        "sleet"          -> "Sludd"
        "thunder"        -> "Torden"
        else             -> symbol
    }
}

@Composable
fun WeatherCard(
    weatherForecast: WeatherForecast?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(horizontal = 6.dp, 6.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
            else if (weatherForecast != null) {

            val cleanSymbol = weatherForecast.weatherSymbol
                .trim()
                .replace(Regex("[^a-zA-Z_]"), "")

            val iconRes = context.resources.getIdentifier(
                weatherForecast.weatherSymbol,
                "drawable",
                context.packageName
            )

              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
              ) {

                  //venstre side av kortet
                  Row(
                      modifier = Modifier.weight(1f),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Image(
                          painter = painterResource(id = if (iconRes != 0) iconRes else R.drawable.cloudy),
                          contentDescription = "Ikon av ${weatherForecast.weatherSymbol}",
                          modifier = Modifier.size(40.dp)
                      )

                      Column(
                          modifier = Modifier.padding(start = 12.dp)
                      ) {
                          Text(
                              text = "${weatherForecast.temperature}°C",
                              style = MaterialTheme.typography.headlineSmall
                          )

                          Text(
                              text = weatherSymbolToDescription(cleanSymbol),
                              style = MaterialTheme.typography.bodyMedium,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                      }
                  }

                  Spacer(modifier = Modifier.width(3.dp))


                  //høyre side av kortet
                  Column(
                      horizontalAlignment = Alignment.End,
                  ) {
                      Text(
                          text = "🌧 ${weatherForecast.precipitation} mm",
                          style = MaterialTheme.typography.bodySmall
                      )
                  }
              }
            }
            else {
                Text(
                    text = "Kunne ikke hente vær",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
    }
}

@Preview (showBackground = true)
@Composable
fun WeatherCardPreview() {
    WeatherCard(
        weatherForecast = WeatherForecast(
            temperature = 12.5,
            weatherSymbol = "partlycloudy_day",
            windSpeed = 3.2,
            precipitation = 0.0
        ),
        isLoading = false
    )
}