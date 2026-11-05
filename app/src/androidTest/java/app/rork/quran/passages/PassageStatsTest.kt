package app.rork.quran.passages

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.data.local.AppDatabase
import app.rork.quran.data.repository.PassageCatalog
import app.rork.quran.data.repository.StatsRepository
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PassageStatsTest {
    @Test fun readingCountsEveryVerseOnceAndOldBookmarksRemainRemovable() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = StatsRepository(database, database.statsDao(), Gson())
            val ids = PassageCatalog(context).resolve("107-4").verseIds
            repository.markAsRead("${java.time.LocalDate.now()}:107-4")
            repository.markPassageRead(ids)
            repository.markPassageRead(ids)
            assertEquals(4, repository.getStats().totalRead)
            assertEquals(4, repository.getStats().dailyHistory.single().count)
            repository.toggleBookmark("107-4")
            repository.toggleBookmark("107-5")
            repository.togglePassageBookmark(ids)
            assertTrue(repository.getStats().bookmarkedIds.isEmpty())
            repository.togglePassageBookmark(ids)
            assertEquals(listOf("107-4"), repository.getStats().bookmarkedIds)
            repository.removePassageBookmark(ids)
            assertTrue(repository.getStats().bookmarkedIds.isEmpty())
        } finally {
            database.close()
        }
    }
}
