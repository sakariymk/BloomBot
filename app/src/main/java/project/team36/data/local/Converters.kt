package project.team36.data.local

import androidx.room.TypeConverter
import project.team36.model.plante.Month
import project.team36.model.plante.LightType
import project.team36.model.plante.SoilType
import project.team36.model.plante.Zone

// konvertere enum-klasser til og fra Strings

class Converters {
    @TypeConverter
    fun fromSoilType(value: SoilType): String = value.name

    @TypeConverter
    fun toSoilType(value: String): SoilType = SoilType.valueOf(value)

    @TypeConverter
    fun fromLightType(value: LightType): String = value.name

    @TypeConverter
    fun toLightType(value: String): LightType = LightType.valueOf(value)

    @TypeConverter
    fun fromMonth(value: Month): String = value.name

    @TypeConverter
    fun toMonth(value: String): Month = Month.valueOf(value)

    @TypeConverter
    fun fromZone(value: Zone): String = value.name

    @TypeConverter
    fun toZone(value: String): Zone = Zone.valueOf(value)
}