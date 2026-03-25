package project.team36.data.plante

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
        val zones = tokens[7].trim().split(";")
        val name = tokens[0].trim().lowercase().replace(" ", "_")
        return PlantInfo(
            name = tokens[0].trim(),
            soil = tokens[1].trim().split(";").map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            isFrostResistant = tokens[3].trim().toBoolean(),
            isIndoor = tokens[4].trim().toBoolean(),
            waterPerWeek = tokens[5].trim().toFloat(),
            plantingMonth = Month.valueOf(tokens[6].trim().uppercase()),
            minZone = Zone.valueOf(zones[0].trim().uppercase()),
            maxZone = Zone.valueOf(zones[1].trim().uppercase()),
            growthdays = tokens[8].trim().toInt(),
            imageRes = context.resources.getIdentifier(name, "drawable", context.packageName),
            beskrivelse = context.assets.open("descriptions/$name.txt").bufferedReader().readText(),
        )
    }}
