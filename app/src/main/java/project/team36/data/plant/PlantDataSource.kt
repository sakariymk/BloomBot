package project.team36.data.plant

import android.content.Context
import project.team36.model.plante.*

class PlantDataSource(private val context: Context) {

    fun fetchPlants(): List<PlantInfo> {
        val plants = mutableListOf<PlantInfo>()

        context.assets.open("planter.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                if (tokens.size >= 11) {
                    plants.add(mapToPlant(tokens))
                }
            }
        }
        return plants
    }
    private fun mapToPlant(tokens: List<String>): PlantInfo {
        val name = tokens[0].trim().lowercase().replace(" ", "_")
        return PlantInfo(
            name = tokens[0].trim(),
            soil = tokens[1].trim().split(";").map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            isFrostResistant = tokens[3].trim().toBoolean(),
            waterPerWeek = tokens[4].trim().toFloat(),
            plantingMonth = tokens[5].trim().split(";").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            climateZone = tokens[6].trim().toInt(),
            hasBeenWatered = tokens[7].trim().toBoolean(),
            daysSinceWatered = tokens[8].trim().toInt(),
            maxDaysWithoutWater = tokens[9].trim().toInt(),
            imageRes = context.resources.getIdentifier(name, "drawable", context.packageName),
            description = context.assets.open("descriptions/$name.txt").bufferedReader().readText(),
        )
    }}
