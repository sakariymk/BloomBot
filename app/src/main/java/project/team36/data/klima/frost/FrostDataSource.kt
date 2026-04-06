package project.team36.data.klima.frost

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import project.team36.model.frost.ObservationResponse
import project.team36.model.frost.SourceResponse
import project.team36.model.frost.Source

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true //
            isLenient = true
        })
    }
}

suspend fun getNearestStation(lat: Double, lon: Double, clientId: String): Source? {
    val url = "https://frost.met.no/sources/v0.jsonld"

    return try {
        val response = client.get(url) {
            // Frost requires our client ID as username in basic auth
            basicAuth(clientId, "")

            //We use the nearest funksjonen of Frost API
            parameter("geometry", "nearest(POINT($lon $lat))")
        }

        if (response.status == HttpStatusCode.OK) {
            val sourceResponse: SourceResponse = response.body()
            // Returns the first station in the list
            sourceResponse.data.firstOrNull()
        } else {
            println("Errors from API: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Did not fetch data: ${e.message}")
        null
    }
}

suspend fun getTemperatures(stationId: String, clientId: String): Double? {
    val url = "https://frost.met.no/observations/v0.jsonld"

    return try {
        val response = client.get(url) {
            basicAuth(clientId, "")
            parameter("sources", stationId)
            parameter("elements", "air_temperature")
            parameter("referencetime", "latest")
        }

        if (response.status == HttpStatusCode.OK) {
            val observationResponse: ObservationResponse = response.body()
            val value = observationResponse.data.firstOrNull()?.observations?.firstOrNull()?.value
            println("Fected value: $value")
            value
        } else {
            null
        }
    } catch (e: Exception) {
        println("Did not manage to fetch temperatues: ${e.message}")
        null
    }
}

suspend fun getPrecipitation(stationId : String, clientId : String, date : String) : Double ? {
    val url = "https://frost.met.no/observations/v0.jsonld"

    return try {
        val response = client.get(url) {
            basicAuth(clientId, "")
            parameter("sources", stationId)
            parameter("elements", "sum(precipitation_amount P1D)")
            parameter("referencetime", date)
        }

        if (response.status == HttpStatusCode.OK) {
            val observationResponse : ObservationResponse = response.body()
            val value = observationResponse.data.firstOrNull()?.observations?.firstOrNull()?.value
            println("Fetched precipitation $value mm at $stationId")
            value
        } else {
            null
        }
    } catch (e : Exception) {
        println("Did not manage to fetch precipitation amount : ${e.message}")
        null
    }

}


suspend fun getDurationSunShine(stationId : String, clientId : String, date : String) : Double ? {
    val url = "https://frost.met.no/observations/v0.jsonld"

    return try {
        val response = client.get(url) {
            basicAuth(clientId, "")
            parameter("sources", stationId)
            parameter("elements", "sum(duration_of_sunshine P1D)")
            parameter("referencetime", date)
        }

        if (response.status == HttpStatusCode.OK) {
            val observationResponse: ObservationResponse = response.body()
            val value = observationResponse.data.firstOrNull()?.observations?.firstOrNull()?.value
            println("Fetched $value hours of sunshine at $stationId")
            value
        } else {
            null
        }
    } catch (e: Exception) {
        println("Did not manage to fetch hours of sunshine : ${e.message}")
        null
    }
}