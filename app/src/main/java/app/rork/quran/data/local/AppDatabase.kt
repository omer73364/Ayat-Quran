package app.rork.quran.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.rork.quran.data.local.dao.AyahDao
import app.rork.quran.data.local.dao.StatsDao
import app.rork.quran.data.local.entity.AyahEntity
import app.rork.quran.data.local.entity.ReadVerseEntity
import app.rork.quran.data.local.entity.StatsEntity

@Database(
    entities = [AyahEntity::class, StatsEntity::class, ReadVerseEntity::class, app.rork.quran.data.local.entity.ReadingEventEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `reading_events` (`token` TEXT NOT NULL, PRIMARY KEY(`token`))")
            }
        }
    }

    abstract fun ayahDao(): AyahDao
    abstract fun statsDao(): StatsDao
}
