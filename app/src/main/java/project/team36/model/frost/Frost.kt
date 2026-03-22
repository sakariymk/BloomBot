package project.team36.model.frost

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceResponse(
    val data: List<Source>,
    val totalItemCount: Int? = null,
    val apiVersion: String? = null
)

@Serializable
data class Source(
    val id: String,
    val name: String,
    val geometry: Point,
    val masl: Double? = null, // Endret til Double for å tåle desimaler
    val municipality: String? = null,
    val county: String? = null
)

@Serializable
data class Point(
    @SerialName("@type") val type: String? = null,
    val coordinates: List<Double>
)

@Serializable
data class TimeSeries (
    val sourceId : String,
    val elementId : String,
    val validFrom : String? = null,
    val validTo : String? = null,
    val unit : String? = null
)

@Serializable
data class TimeSeriesResponse (
    val data : List <TimeSeries>
)

@Serializable
data class Observation (
    val sourceId : String,
    val referenceTime: String,
    val observations : List <ObservationValue>
)


@Serializable
data class ObservationValue (
    val elementId : String,
    val value : Double,
    val unit : String,
    val timeOffset : String ? = null

)

@Serializable
data class ObservationResponse (
    val data : List <Observation>
)