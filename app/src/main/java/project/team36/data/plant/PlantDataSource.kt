package project.team36.data.plant

import android.content.Context
import project.team36.model.plant.*

/**
 * Formål og funksjon:
 * Dette er data-uthenteren min. Den leser CSV-filen vår og oversetter råteksten
 * til de smarte PlanteInfo-objektene jeg har definert. Jeg har bygget inn robuste
 * "parsemotorer" her som tåler feil i CSV-filen uten at appen krasjer.
 */
class PlantDataSource(private val context: Context) {

    fun fetchPlants(): List<PlantInfo> {
        val plants = mutableListOf<PlantInfo>()

        context.assets.open("planter.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                // Jeg sikrer at linjen har nok kolonner før jeg prøver å lese den
                if (tokens.size >= 8) {
                    plants.add(mapToPlant(tokens))
                }
            }
        }
        return plants
    }

    private fun mapToPlant(tokens: List<String>): PlantInfo {
        val name = tokens[0].trim().lowercase().replace(" ", "_")

        // Henter sone-teksten fra CSV-en (f.eks "H1;H4" -> jeg tar "H4")
        val zones = tokens[7].trim().split(";")
        val maxZoneString = if (zones.size > 1) zones[1] else zones[0]

        // Jeg regner ut hvor mange dager planten klarer seg uten vann,
        // basert på den gamle verdien 'waterPerWeek' (token 5).
        val waterPerWeek = tokens.getOrNull(5)?.trim()?.toFloatOrNull() ?: 1f
        val calculatedMaxDays = if (waterPerWeek > 0) (7.0 / waterPerWeek).toInt() else 7

        return PlantInfo(
            name = tokens[0].trim(),
            soil = tokens[1].trim().split(";").map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            isFrostResistant = tokens[3].trim().toBoolean(),

            // Jeg bruker egne hjelpefunksjoner for å trygt oversette måned og sone til rene tall
            plantingMonth = parseMonth(tokens[6].trim()),
            climateZone = parseZone(maxZoneString),

            imageRes = context.resources.getIdentifier(name, "drawable", context.packageName),
            beskrivelse = try {
                context.assets.open("descriptions/$name.txt").bufferedReader().readText()
            } catch (e: Exception) {
                "Ingen beskrivelse tilgjengelig."
            },

            // Setter standardverdier for vanning når planten lastes inn første gang
            hasBeenWatered = false,
            daysSinceLastWaterDay = 0,
            maxDaysWithoutWater = calculatedMaxDays
        )
    }

    // Hjelpefunksjon: Konverterer både tall ("4") og tekst ("APR", "APRIL") til riktig månedsnummer (Int).
    private fun parseMonth(input: String): Int {
        return input.toIntOrNull() ?: when(input.uppercase().take(3)) {
            "JAN" -> 1; "FEB" -> 2; "MAR" -> 3; "APR" -> 4;
            "MAY", "MAI" -> 5; "JUN" -> 6; "JUL" -> 7; "AUG" -> 8;
            "SEP" -> 9; "OCT", "OKT" -> 10; "NOV" -> 11; "DEC", "DES" -> 12
            else -> 5 // Standard fallback til mai hvis CSV-en mangler data
        }
    }

    // Hjelpefunksjon: Fjerner bokstaven "H" fra sonen hvis den finnes ("H4" -> 4), og returnerer som Int.
    private fun parseZone(input: String): Int {
        return input.replace("H", "", ignoreCase = true).trim().toIntOrNull() ?: 1
    }
}