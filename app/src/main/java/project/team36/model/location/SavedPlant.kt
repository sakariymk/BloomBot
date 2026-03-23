package project.team36.model.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.ktor.util.date.Month
import project.team36.model.plante.LightType
import project.team36.model.plante.SoilType
import project.team36.model.plante.Zone

@Entity(
    tableName = "saved_plants",
    foreignKeys = [ForeignKey(
        entity = SavedLocation::class,
        parentColumns = ["id"],
        childColumns = ["locationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["locationId"])]
)
data class SavedPlant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val name: String,
    val soil: SoilType,
    val light: LightType,
    val isFrostResistant: Boolean,
    val isIndoor: Boolean,
    val waterPerWeek: Float,
    val plantingMonth: Month,
    val minZone: Zone,
    val maxZone: Zone,
    val growthdays: Int, //kan vi pls endre i data classen til growthDays
    val imageRes: Int,
    val beskrivelse: String, //kan vi pls endre til description
)