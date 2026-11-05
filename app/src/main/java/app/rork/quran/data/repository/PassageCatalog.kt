package app.rork.quran.data.repository

import android.content.Context
import app.rork.quran.domain.model.Ayah
import app.rork.quran.domain.model.PassageVerse
import org.json.JSONArray

/** Whole-ayah groups derived from QSU semantic-unit links. Available before unlock. */
class PassageCatalog(private val context: Context) {
    private data class Index(val byVerse: Map<String, Ayah>, val passages: List<Ayah>)
    companion object {
        @Volatile private var cached: Index? = null
    }

    private fun index(): Index = cached ?: synchronized(PassageCatalog::class.java) {
        cached ?: load().also { cached = it }
    }

    fun resolve(id: String): Ayah = index().byVerse.getValue(id.replace(':', '-'))

    fun reminderPassages(): List<Ayah> = index().passages

    // Old reminder payloads retain their ayah identity under the current boundaries.
    fun forReminder(id: String): Ayah = resolve(id)

    private fun load(): Index {
        val source = JSONArray(context.assets.open("ayahs.json").bufferedReader().use { it.readText() })
        val singles = (0 until source.length()).associate { i ->
            val item = source.getJSONObject(i)
            item.getString("id") to Ayah(item.getString("id"), item.getInt("surahNumber"),
                item.getString("surahNameArabic"), item.getInt("ayahNumber"), item.getString("textArabic"))
        }
        val ranges = JSONArray(context.assets.open("passages.json").bufferedReader().use { it.readText() })
        val byVerse = mutableMapOf<String, Ayah>()
        val passages = (0 until ranges.length()).map { i ->
            val range = ranges.getJSONObject(i)
            val surah = range.getInt("surah")
            val start = range.getInt("startAyah")
            val end = range.getInt("endAyah")
            require(start <= end)
            val verses = (start..end).map { number ->
                val verse = singles.getValue("$surah-$number")
                PassageVerse(verse.id, number, verse.textArabic)
            }
            val first = singles.getValue("$surah-$start")
            val passage = first.copy(verses = verses, textArabic = if (verses.size == 1) first.textArabic
                else verses.joinToString("\n") { "${it.text} ﴿${it.number}﴾" })
            verses.forEach { require(byVerse.put(it.id, passage) == null) }
            passage
        }
        check(byVerse.keys == singles.keys) { "Passage boundaries must cover every ayah exactly once" }
        return Index(byVerse, passages.also { check(it.isNotEmpty()) })
    }
}
