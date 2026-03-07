package app.rork.quran.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.rork.quran.data.local.dao.AyahDao
import app.rork.quran.data.local.dao.StatsDao
import app.rork.quran.data.local.entity.AyahEntity
import app.rork.quran.data.local.entity.ReadVerseEntity
import app.rork.quran.data.local.entity.StatsEntity

@Database(
    entities = [AyahEntity::class, StatsEntity::class, ReadVerseEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ayahDao(): AyahDao
    abstract fun statsDao(): StatsDao
}
