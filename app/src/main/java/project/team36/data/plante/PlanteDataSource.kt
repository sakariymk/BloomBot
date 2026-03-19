package project.team36.data.plante

import android.content.Context
import project.team36.model.plante.*

/**
 * FIL: PlanteDataSource.kt
 * TYPE: KLASSE
 */
class PlantDataSource(private val context: Context) {

    fun fetchPlants(): List<PlanteInfo> {
        val plants = mutableListOf<PlanteInfo>()

        try {
            context.assets.open("planter.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    if (line.isNotBlank()) {
                        val tokens = line.split(",")
                        // Sjekker at vi har nok kolonner før vi prøver å mappe
                        if (tokens.size >= 10) {
                            try {
                                plants.add(mapToPlant(tokens))
                            } catch (e: Exception) {
                                println("Kunne ikke mappe linje: $line. Feil: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Feil ved lesing av CSV: ${e.message}")
        }
        return plants
    }

    private fun mapToPlant(tokens: List<String>): PlanteInfo {
        // HENTER SONER SIKKERT
        val zonesRaw = tokens[8].trim().split(";")

        // Hvis vi har to verdier (H1;H4), bruk begge.
        // Hvis vi bare har én (H2), bruk den som både min og max.
        val minZoneStr = zonesRaw[0].trim().uppercase()
        val maxZoneStr = if (zonesRaw.size > 1) zonesRaw[1].trim().uppercase() else minZoneStr

        return PlanteInfo(
            name = tokens[0].trim(),
            soil = tokens[1].trim().split(";").filter { it.isNotBlank() }
                .map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").filter { it.isNotBlank() }
                .map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            isFrostResistant = tokens[3].trim().toBoolean(),
            isIndoor = tokens[4].trim().toBoolean(),
            minTemp = tokens[5].trim().toIntOrNull() ?: 0,
            waterPerWeek = tokens[6].trim().toFloatOrNull() ?: 0f,
            plantingSeason = try {
                Season.valueOf(tokens[7].trim().uppercase())
            } catch(e: Exception) {
                Season.SPRING
            },
            minZone = Zone.valueOf(minZoneStr),
            maxZone = Zone.valueOf(maxZoneStr),
            humidityLevel = tokens[9].trim().toIntOrNull() ?: 5
        )
    }
}