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
        assertEquals((1..7).map { "107-$it" }, passage.verseIds)
        assertTrue(passage.textArabic.contains("﴿4﴾"))
        assertTrue(passage.textArabic.contains("﴿7﴾"))
        assertEquals(passage, catalog.resolve("107-5"))
        assertEquals("الآيات 1–7", passage.reference)
    }

    @Test fun remindersUseWholeShortPassagesAndLegacyJsonResolvesToTheSameGroup() {
        val reminders = catalog.reminderPassages()
        assertTrue(reminders.isNotEmpty())
        reminders.forEach {
            assertTrue(it.textArabic.length <= 1200)
            assertTrue(it.verseIds.size <= 12)
            assertEquals(catalog.resolve(it.id), it)
        }
        val legacy = Gson().fromJson("""{"id":"107-4","surahNumber":107,"surahNameArabic":"الماعون","ayahNumber":4,"textArabic":"قديم"}""", Ayah::class.java)
        assertEquals(catalog.resolve("107-4"), catalog.forReminder(legacy.id))
        val grouped = catalog.resolve("107-4")
        assertEquals(grouped, Gson().fromJson(Gson().toJson(grouped), Ayah::class.java))
    }
}
