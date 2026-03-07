package app.rork.quran.data.repository

import app.rork.quran.data.local.dao.StatsDao
import app.rork.quran.data.local.entity.StatsEntity
import app.rork.quran.domain.model.DailyRead
import app.rork.quran.domain.model.ReadingStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val statsDao: StatsDao,
    private val gson: Gson,
) {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeStats(): Flow<ReadingStats> =
        statsDao.observeStats().map { it?.toDomain() ?: ReadingStats() }

    suspend fun getStats(): ReadingStats =
        statsDao.getStats()?.toDomain() ?: ReadingStats()

    suspend fun markAsRead() {
        val stats = getStats()
        val today = LocalDate.now().format(dateFormat)
        val history = stats.dailyHistory.toMutableList()
        val idx = history.indexOfFirst { it.date == today }
        if (idx >= 0) {
            history[idx] = history[idx].copy(count = history[idx].count + 1)
        } else {
            history.add(DailyRead(today, 1))
        }
        val newStats = stats.copy(
            totalRead = stats.totalRead + 1,
            lastReadDate = today,
            dailyHistory = history,
            streak = calculateStreak(history),
        )
        statsDao.upsertStats(newStats.toEntity())
    }

    suspend fun toggleBookmark(ayahId: String) {
        val stats = getStats()
        val bookmarks = stats.bookmarkedIds.toMutableList()
        if (bookmarks.contains(ayahId)) bookmarks.remove(ayahId) else bookmarks.add(ayahId)
        statsDao.upsertStats(stats.copy(bookmarkedIds = bookmarks).toEntity())
    }

    private fun calculateStreak(history: List<DailyRead>): Int {
        if (history.isEmpty()) return 0
        val sorted = history.sortedByDescending { it.date }
        val today = LocalDate.now().format(dateFormat)
        val yesterday = LocalDate.now().minusDays(1).format(dateFormat)
        if (sorted[0].date != today && sorted[0].date != yesterday) return 0
        var streak = 0
        var checkDate = LocalDate.parse(sorted[0].date, dateFormat)
        for (entry in sorted) {
            if (entry.date == checkDate.format(dateFormat)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else break
        }
        return streak
    }

    private fun StatsEntity.toDomain(): ReadingStats {
        val historyType = object : TypeToken<List<DailyRead>>() {}.type
        val bookmarksType = object : TypeToken<List<String>>() {}.type
        return ReadingStats(
            totalRead = totalRead,
            streak = streak,
            lastReadDate = lastReadDate,
            dailyHistory = gson.fromJson(dailyHistoryJson, historyType) ?: emptyList(),
            bookmarkedIds = gson.fromJson(bookmarkedIdsJson, bookmarksType) ?: emptyList(),
        )
    }

    private fun ReadingStats.toEntity() = StatsEntity(
        id = 0,
        totalRead = totalRead,
        streak = streak,
        lastReadDate = lastReadDate,
        dailyHistoryJson = gson.toJson(dailyHistory),
        bookmarkedIdsJson = gson.toJson(bookmarkedIds),
    )
}
