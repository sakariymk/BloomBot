package project.team36.data.plant

import android.content.Context
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantDataSource(private val context: Context) {

    fun fetchPlants(): List<PlantInfo> {
        val plants = mutableListOf<PlantInfo>()
        var descIndex = 0

        context.assets.open("planter.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split("/")
                if (tokens.size >= 10) {
                    plants.add(mapToPlant(tokens))
                }
            }
        }
        return plants
    }
    private fun mapToPlant(tokens: List<String>): PlantInfo {
        val rawName = tokens[0].trim()
        val normalizedName = normalizeName(rawName)  // kun til imageRes-oppslag
        return PlantInfo(
            name = rawName,  // vis originalnavnet til brukeren
            soil = tokens[1].trim().split(";").map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            isFrostResistant = tokens[3].trim().toBoolean(),
            waterPerWeek = tokens[4].trim().toFloat(),
            plantingMonth = tokens[5].trim().split(";").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            climateZone = tokens[6].trim().toInt(),
            hasBeenWatered = tokens[7].trim().toBoolean(),
            daysSinceWatered = tokens[8].trim().toInt(),
            maxDaysWithoutWater = tokens[9].trim().toInt(),
            imageRes = context.resources.getIdentifier(normalizedName, "drawable", context.packageName),
            description = tokens[10],
        )
    }
    private fun normalizeName(name: String): String {
        return name
            .replace("æ", "ae").replace("Æ", "ae")
            .replace("ø", "oe").replace("Ø", "oe")
            .replace("å", "aa").replace("Å", "aa")
            .lowercase()
            .replace(" ", "_")
    }
}
