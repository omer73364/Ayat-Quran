package app.rork.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.rork.quran.data.local.entity.AyahEntity
import app.rork.quran.data.local.entity.ReadVerseEntity

@Dao
interface AyahDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ayahs: List<AyahEntity>)

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun count(): Int

    @Query("SELECT * FROM ayahs WHERE id NOT IN (SELECT ayahId FROM read_verses) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnreadAyah(): AyahEntity?

    @Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomAyah(): AyahEntity

    @Query("SELECT * FROM ayahs WHERE id = :id")
    suspend fun getAyahById(id: String): AyahEntity?

    @Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomAyahs(count: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markAsRead(entity: ReadVerseEntity)

    @Query("SELECT COUNT(*) FROM read_verses")
    suspend fun readCount(): Int

    @Query("DELETE FROM read_verses")
    suspend fun resetReadVerses()
}
