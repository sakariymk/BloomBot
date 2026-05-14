package project.team36.model.frost

data class WeatherData (
    val stationId : String,
    val temperature : Double?,
    val precipitation : Double?,
    val sunshineHours : Double?
)
