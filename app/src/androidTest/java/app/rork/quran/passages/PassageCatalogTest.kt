package app.rork.quran.passages

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.data.repository.PassageCatalog
import app.rork.quran.domain.model.Ayah
import com.google.gson.Gson
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PassageCatalogTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val catalog = PassageCatalog(context)

    @Test fun everyAyahBelongsToExactlyOneContiguousPassageInItsSurah() {
        val source = JSONArray(context.assets.open("ayahs.json").bufferedReader().use { it.readText() })
        val verses = (0 until source.length()).map { source.getJSONObject(it).getString("id") }.toSet()
        val passages = verses.map(catalog::resolve).distinctBy { it.id }
        val covered = passages.flatMap { it.verseIds }
        assertEquals(6236, covered.size)
        assertEquals(verses, covered.toSet())
        passages.forEach { passage ->
            val members = passage.verses.orEmpty()
            assertEquals((members.first().number..members.last().number).toList(), members.map { it.number })
            assertTrue(members.all { it.id.startsWith("${passage.surahNumber}-") })
            members.forEach { assertEquals(passage, catalog.resolve(it.id)) }
        }
    }

    @Test fun alMaunAlwaysIncludesTheContextInsteadOfAnIsolatedWarning() {
        val passage = catalog.resolve("107-4")
        assertEquals((4..7).map { "107-$it" }, passage.verseIds)
        assertTrue(passage.textArabic.contains("﴿4﴾"))
        assertTrue(passage.textArabic.contains("﴿7﴾"))
        assertEquals(passage, catalog.resolve("107-5"))
        assertEquals("الآيات 4–7", passage.reference)
    }


    @Test fun singleAyahsAndLongerExceptionsFollowTheSourceRatherThanSurahLength() {
        assertEquals(listOf("1-1"), catalog.resolve("1-1").verseIds)
        assertEquals((2..4).map { "1-$it" }, catalog.resolve("1-3").verseIds)
        assertEquals(listOf("107-1"), catalog.resolve("107-1").verseIds)
        assertEquals(listOf("107-2", "107-3"), catalog.resolve("107-3").verseIds)
        assertEquals((1..6).map { "114-$it" }, catalog.resolve("114-6").verseIds)
    }

    @Test fun remindersPreserveEveryGroupIncludingLongExceptionsAndLegacyPayloads() {
        val reminders = catalog.reminderPassages()
        assertEquals(4567, reminders.size)
        assertEquals(3636, reminders.count { it.verseIds.size == 1 })
        assertEquals(885, reminders.count { it.verseIds.size in 2..5 })
        assertEquals(46, reminders.count { it.verseIds.size > 5 })
        assertEquals((1..114).toSet(), reminders.map { it.surahNumber }.toSet())
        val long = catalog.forReminder("53-40")
        assertEquals((36..52).map { "53-$it" }, long.verseIds)
        assertTrue(reminders.contains(long))
        reminders.forEach {
            assertEquals(catalog.resolve(it.id), it)
        }
        val legacy = Gson().fromJson("""{"id":"107-4","surahNumber":107,"surahNameArabic":"الماعون","ayahNumber":4,"textArabic":"قديم"}""", Ayah::class.java)
        assertEquals(catalog.resolve("107-4"), catalog.forReminder(legacy.id))
        val grouped = catalog.resolve("107-4")
        assertEquals(grouped, Gson().fromJson(Gson().toJson(grouped), Ayah::class.java))
    }
}
