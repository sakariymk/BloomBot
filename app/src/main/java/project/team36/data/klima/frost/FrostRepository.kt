package project.team36.data.klima.frost

class FrostRepository {
    private val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

    suspend fun getPrecipitationSinceLastUsed(
        lat: Double,
        lon: Double,
        lastWatered: String
    ): Map<String, Double>? {
        return getPrecipitationSinceLastUsed(lat, lon, clientId, lastWatered)
    }
}