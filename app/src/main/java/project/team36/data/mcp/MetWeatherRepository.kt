package project.team36.data.mcp

import android.util.Log
import org.json.JSONObject

data class WeatherForecast(
    val temperature: Double,
    val weatherSymbol: String,
    val windSpeed: Double,
    val precipitation: Double
)

class MetWeatherRepository (private val mcpClient: MetMcpClient) {
    //TODO: lage ulike metoder og dataklasser(?) for å hente info direkte fra MCP

    //For værvarsel på location
    suspend fun getParsedForecast(lat: Double, lon: Double): WeatherForecast?{
        return try {
            val text = mcpClient.getForecast(lat, lon)

            return WeatherForecast(
                temperature = Regex("Temperature: ([\\d.]+) °C").find(text)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0,
                windSpeed = Regex("Wind: ([\\d.]+) m/s").find(text)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0,
                weatherSymbol = Regex("Weather symbol: (\\S+)").find(text)
                    ?.groupValues?.get(1)
                    ?.substringBeforeLast("\\n\\n##")
                    ?: "",
                precipitation = Regex("Precipitation: ([\\d.]+) mm").find(text)
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
        Log.d("MET_ALERTS_RAW", rawText)
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
            val regex = Regex("$key:\\s*([\\s\\S]*?)(?=\\n\\s*#|\\n\\s*###|\\n\\s*####|$)")
            return regex.find(block)?.groupValues?.get(1)?.trim()
                ?.replace(Regex("\\n\\s*"), " ") ?: ""
        }

        return blocks.mapNotNull { block ->
            val event = extract(block, "# Event").ifBlank { return@mapNotNull null }
            val severity = extract(block, "Severity").ifBlank { "Unknown" }
            val description = extract(block, "Description").substringBefore("Consequences: ").trim()
            val area = extract(block, "### Area")

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

            WeatherAlert(event, severity, description, area, polygons)
        }
    }

    fun isPointInPolygon(lat: Double, lon: Double, polygon: List<Pair<Double, Double>>): Boolean {
        var inside = false
        var j = polygon.lastIndex
        for (i in polygon.indices) {
            val xi = polygon[i].second; val yi = polygon[i].first
            val xj = polygon[j].second; val yj = polygon[j].first

            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lon < (xj - xi) * (lat - yi) / (yj - yi + 0.00000001) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}

data class WeatherAlert(
    val event: String,
    val severity: String,      // f.eks. "Moderat", "Alvorlig", "Ekstrem"
    val description: String,
    val area : String = "",
    val polygons: List<List<Pair<Double, Double>>> = emptyList()

)

