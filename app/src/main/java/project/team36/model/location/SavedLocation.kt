package project.team36.model.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_location")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "latitude") val lat: Double?,
    @ColumnInfo(name = "longitude") val lon: Double?,
    @ColumnInfo(name = "address") val address: String?
)