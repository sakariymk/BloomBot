package project.team36.data.mcp

import android.util.Log
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import project.team36.ui.theme.AlertOrange
import project.team36.ui.theme.AlertRed
import project.team36.ui.theme.AlertYellow

data class WeatherForecast(
    val temperature: Double,
    val weatherSymbol: String,
    val windSpeed: Double,
    val precipitation: Double
)

class MetWeatherRepository (private val mcpClient: MetMcpClient) {
    //For værvarsel på location for å kunne bruke det til værvarsel
    suspend fun getParsedForecast(lat: Double, lon: Double): WeatherForecast? {
        return try {
            val text = mcpClient.getForecast(lat, lon)

            /*  Splitte responsen i blokker per time.
                ?= Lookahead, ser på mønsteret
                ## matcher overskriften
                Resten matcher stor forbokstav og to små bokstaver for ukedag i responsen.
            */
            val blocks = text.split(Regex("(?=## [A-Z][a-z]{2} [A-Z][a-z]{2})"))
                .filter { it.contains("Weather symbol:") }

            // Velg blokken for første time
            val targetBlock = blocks.firstOrNull() ?: return null

            //Pattern matching på resten av forecasten, henter det som er i blokkene spesifisert for videre bruk.
            WeatherForecast(
                temperature = Regex("Temperature: ([\\d.]+) °C").find(targetBlock)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0,
                windSpeed = Regex("Wind: ([\\d.]+) m/s").find(targetBlock)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0,
                weatherSymbol = Regex("Weather symbol: ([a-z_]+)").find(targetBlock)
                    ?.groupValues?.get(1)
                    ?: "",
                precipitation = Regex("Precipitation: ([\\d.]+) mm").find(targetBlock)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("WEATHER", "Failed to fetch weather: ${e.message}")
            null
        }
    }

    suspend fun getAllAlerts(): List<WeatherAlert> {
        mcpClient.connect()
        val rawText = mcpClient.getAlerts()

        if (rawText.isBlank() || rawText.startsWith("Kunne ikke")) return emptyList()
        val jsonText = try {
            JSONObject(rawText).getString("alerts")
        } catch (e: Exception) {
            rawText
        }
        return parseAlerts(jsonText)
    }

    private fun parseAlerts(text: String): List<WeatherAlert> {
        val blocks = text.split(Regex("(?=#\\s*Event:)")).filter { it.isNotBlank() }

        fun extract(block: String, key: String): String {
            val regex = Regex("$key:\\s*([\\s\\S]*?)(?=\\n\\s*[A-Z][a-z]+:|\\n\\s*#|$)")
            return regex.find(block)?.groupValues?.get(1)?.trim()
                ?.replace(Regex("\\s+"), " ") ?: ""
        }

        return blocks.mapNotNull { block ->
            val event = extract(block, "Event").ifBlank { extract(block, "# Event") }
            if (event.isBlank()) return@mapNotNull null

            val severity = extract(block, "Severity").ifBlank { "Unknown" }
            val awarenessLevel = extract(block, "Awareness level").ifBlank { "Unknown" }

            // Fjerner alt før "Consequences" eller "Instruction"
            val rawDescription = extract(block, "Description")
            val cleanDescription = rawDescription
                .split("Consequences:")[0]
                .split("Instruction:")[0]
                .trim()

            val area = extract(block, "Area")

            val polygons = Regex("#### Polygons[\\s\\S]*?(?=###|# Event|$)")
                .find(block)?.value
                ?.let { section ->
                    Regex("-\\s*([\\d.,\\s]+)").findAll(section).map { match ->
                        match.groupValues[1].trim().split(Regex("\\s+"))
                            .mapNotNull { pair ->
                                val parts = pair.split(",")
                                if (parts.size == 2) {
                                    val pLat = parts[0].toDoubleOrNull()
                                    val pLon = parts[1].toDoubleOrNull()
                                    if (pLat != null && pLon != null) Pair(pLat, pLon) else null
                                } else null
                            }
                    }.map { it.toList() }.toList()
                } ?: emptyList()

            WeatherAlert(event, severity, cleanDescription, awarenessLevel, area, polygons)
        }
    }

    fun isPointInPolygon(lat: Double, lon: Double, polygon: List<Pair<Double, Double>>): Boolean {
        if (polygon.isEmpty()) return false

        var intersectCount = 0
        for (i in 0 until polygon.size) {
            val p1 = polygon[i]
            val p2 = polygon[(i + 1) % polygon.size] // Dette håndterer lukking av polygonet automatisk

            if (((p1.first > lat) != (p2.first > lat)) &&
                (lon < (p2.second - p1.second) * (lat - p1.first) / (p2.first - p1.first) + p1.second)
            ) {
                intersectCount++
            }
        }
        return intersectCount % 2 != 0
    }
}

data class WeatherAlert(
    val event: String,
    val severity: String,
    val description: String,
    val awarenessLevel: String,
    val area: String = "",
    val polygons: List<List<Pair<Double, Double>>> = emptyList()
) {
    val color: Color
        get() = when {
            awarenessLevel.contains("Red", ignoreCase = true) -> AlertRed
            awarenessLevel.contains("Orange", ignoreCase = true) -> AlertOrange
            else -> AlertYellow // Gul
        }
}