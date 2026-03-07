package app.rork.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_verses")
data class ReadVerseEntity(
    @PrimaryKey val ayahId: String,
)
