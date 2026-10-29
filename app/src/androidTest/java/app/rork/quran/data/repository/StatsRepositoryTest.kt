package app.rork.quran.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.rork.quran.data.local.AppDatabase
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatsRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: StatsRepository

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        repository = StatsRepository(database, database.statsDao(), Gson())
    }

    @After fun tearDown() { database.close() }

    @Test fun duplicateReadActionsCountOnce() = runBlocking {
        (1..20).map { async { repository.markAsRead("2026-10-22:1:1") } }.awaitAll()
        assertEquals(1, repository.getStats().totalRead)
        assertEquals(1, repository.getStats().dailyHistory.single().count)
    }

    @Test fun concurrentBookmarksAndReadingsPreserveBoth() = runBlocking {
        (1..20).flatMap { index -> listOf(
            async { repository.markAsRead("2026-10-22:1:$index") },
            async { repository.toggleBookmark("1:$index") },
        ) }.awaitAll()
        assertEquals(20, repository.getStats().totalRead)
        assertEquals(20, repository.getStats().bookmarkedIds.size)
    }

    @Test fun removingBookmarkTwiceDoesNotAddItBack() = runBlocking {
        repository.toggleBookmark("1:1")
        repository.removeBookmark("1:1")
        repository.removeBookmark("1:1")
        assertTrue(repository.getStats().bookmarkedIds.isEmpty())
    }
    @Test fun upgradeKeepsBookmarksAndHistory() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "migration-${java.util.UUID.randomUUID()}"
        try {
            val old = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val oldRepository = StatsRepository(old, old.statsDao(), Gson())
            oldRepository.toggleBookmark("1:1")
            oldRepository.markAsRead("migration-reading")
            old.openHelper.writableDatabase.execSQL("DROP TABLE reading_events")
            old.openHelper.writableDatabase.execSQL("PRAGMA user_version = 1")
            old.close()
            val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_1_2).build()
            try {
                val upgradedRepository = StatsRepository(upgraded, upgraded.statsDao(), Gson())
                assertEquals(listOf("1:1"), upgradedRepository.getStats().bookmarkedIds)
                assertEquals(1, upgradedRepository.getStats().totalRead)
                upgradedRepository.markAsRead("new-reading")
                upgradedRepository.markAsRead("new-reading")
                assertEquals(2, upgradedRepository.getStats().totalRead)
            } finally { upgraded.close() }
        } finally { context.deleteDatabase(name) }
    }

}
