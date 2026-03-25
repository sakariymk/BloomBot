package project.team36.model.plante

import kotlinx.serialization.Serializable

enum class SoilType { SOIL, CHALK, CLAY, LOAM, SAND }
enum class LightType { SHADE, PARTIAL, FULL }
enum class Month { JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC }

enum class Zone { H1, H2, H3, H4, H5, H6, H7, H8}

@Serializable
data class PlantInfo(
    val name: String,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val isFrostResistant: Boolean,
    val isIndoor: Boolean,
    val waterPerWeek: Float,
    val plantingMonth: Month,
    val minZone: Zone,
    val maxZone: Zone,
    val growthdays: Int,
    val imageRes: Int,
    val beskrivelse: String,
    val humidityLevel: Int,
)