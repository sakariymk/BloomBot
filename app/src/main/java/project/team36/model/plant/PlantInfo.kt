package project.team36.model.plant

import kotlinx.serialization.Serializable

enum class SoilType { SOIL, CHALK, CLAY, LOAM, SAND }
enum class LightType { SHADE, PARTIAL, FULL }

/**
 * Formål og funksjon:
 * Jeg har designet datamodellen for plantene våre. Jeg valgte bevisst å gå vekk
 * fra komplekse Enums for måneder og klimasoner, og heller bruke rene heltall (Int).
 * Dette gjør det betydelig enklere å skrive logikk for sammenligning (f.eks. sjekke
 * om brukerens sone 3 er innenfor plantens makssone 5).
 */
@Serializable
data class PlantInfo(
    val name: String,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val isFrostResistant: Boolean,

    // Jeg bruker 1 = Januar, 12 = Desember
    val plantingMonth: Int,

    // Jeg bruker 1 = H1, 8 = H8
    val climateZone: Int,

    val imageRes: Int,
    val beskrivelse: String,

    // Disse er 'var' fordi appen må kunne oppdatere dem når brukeren vanner planten
    var hasBeenWatered: Boolean = false,
    var daysSinceLastWaterDay: Int = 0,
    val maxDaysWithoutWater: Int
) {
    // LOGIKK:  lagde denne for å returnere et tidsvindu på +- 1 måned for planting.
    // Den håndterer automatisk overgangen mellom årsskifter (Desember -> Januar).
    fun getPlantingMonthsSpan(): List<Int> {
        val prevMonth = if (plantingMonth == 1) 12 else plantingMonth - 1
        val nextMonth = if (plantingMonth == 12) 1 else plantingMonth + 1
        return listOf(prevMonth, plantingMonth, nextMonth)
    }

    // LOGIKK: Itererer opp til sone 1. Siden jeg bruker rene tall,
    // blir logikken for å sjekke om planten overlever i brukerens sone ekstremt elegant.
    fun supportsClimateZone(userZone: Int): Boolean {
        // Hvis planten tåler sone 4, vil den også tåle sone 1, 2 og 3.
        return userZone <= climateZone
    }

    // LOGIKK: Hjelpefunksjon jeg kaller når planten får vann (enten fra regn eller bruker).
    fun waterPlant() {
        hasBeenWatered = true
        daysSinceLastWaterDay = 0
    }
}