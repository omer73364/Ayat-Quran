package app.rork.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.rork.quran.data.local.entity.StatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM reading_stats WHERE id = 0")
    fun observeStats(): Flow<StatsEntity?>

    @Query("SELECT * FROM reading_stats WHERE id = 0")
    suspend fun getStats(): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: StatsEntity)
}
