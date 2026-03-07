package app.rork.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_stats")
data class StatsEntity(
    @PrimaryKey val id: Int = 0, // single row
    val totalRead: Int,
    val streak: Int,
    val lastReadDate: String,
    val dailyHistoryJson: String,   // JSON: List<DailyRead>
    val bookmarkedIdsJson: String,  // JSON: List<String>
)
