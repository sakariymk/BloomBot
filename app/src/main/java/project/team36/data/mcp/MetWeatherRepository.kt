package project.team36.data.mcp

import android.util.Log

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
                    ?.groupValues?.get(1) ?: "",
                precipitation = Regex("Precipitation: ([\\d.]+) mm").find(text)
                    ?.groupValues?.get(1)?.toDouble() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("WEATHER", "Failed to fetch weather: ${e.message}")
            null
        }
    }

    suspend fun getAlertForLocation() {
        //TODO
    }
}