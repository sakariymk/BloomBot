package project.team36.data.klima.frost
import io.ktor.client.HttpClient
import project.team36.model.frost.WeatherData

class FrostRepository {
    private val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

    suspend fun getFullWeatherForLocation (lat : Double, lon : Double, date : String = "latest") : WeatherData? {
        val station = getNearestStation(lat, lon, clientId) ?: return null
        return WeatherData (
            stationId = station.id,
            temperature = getTemperatures(station.id, clientId),
            precipitation = getPrecipitation (station.id, clientId, date),
            sunshineHours = getDurationSunShine(station.id, clientId, date)
        )

    }

    suspend fun getPrecipitationSinceLastUsed(
        lat: Double,
        lon: Double,
        lastWatered: String
    ): Map<String, Double>? {
        return getPrecipitationSinceLastUsed(lat, lon, clientId, lastWatered)
    }
}
