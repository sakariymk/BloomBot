package project.team36.model.plante

import kotlinx.serialization.Serializable

enum class SoilType { SOIL, CHALK, CLAY, LOAM, SAND }
enum class LightType { SHADE, PARTIAL, FULL }
enum class Season { SPRING, SUMMER, AUTUMN, WINTER }

enum class Zone { H1, H2, H3, H4, H5, H6, H7, H8}

@Serializable
data class PlanteInfo(
    val name: String,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val isFrostResistant: Boolean,
    val isIndoor: Boolean,
    val minTemp: Int,
    val waterPerWeek: Float,
    val plantingSeason: Season,
    val minZone: Zone,
    val maxZone: Zone,
    val humidityLevel: Int)