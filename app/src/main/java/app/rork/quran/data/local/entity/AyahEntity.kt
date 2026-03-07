package app.rork.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val textArabic: String,
)
