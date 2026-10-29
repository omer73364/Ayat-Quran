package app.rork.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_events")
data class ReadingEventEntity(@PrimaryKey val token: String)
