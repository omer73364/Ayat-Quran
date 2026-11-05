package app.rork.quran.widget

import android.content.Context
import app.rork.quran.domain.model.Ayah
import java.time.LocalDate

/** Each widget keeps its displayed verse across process death and launcher recreation. */
internal class AyahWidgetStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("ayah_widgets", Context.MODE_PRIVATE)

    fun load(id: Int): Ayah? {
        val verseId = prefs.getString("${id}_id", null) ?: return null
        return app.rork.quran.data.repository.PassageCatalog(context).resolve(verseId)
    }

    fun isCurrent(id: Int): Boolean = prefs.getString("${id}_date", null) == LocalDate.now().toString()

    fun save(id: Int, ayah: Ayah) {
        prefs.edit().putString("${id}_id", ayah.id).putInt("${id}_surah", ayah.surahNumber)
            .putString("${id}_name", ayah.surahNameArabic).putInt("${id}_number", ayah.ayahNumber)
            .putString("${id}_text", ayah.textArabic).putString("${id}_date", LocalDate.now().toString()).apply()
    }

    fun remove(id: Int) {
        val edit = prefs.edit()
        listOf("id", "surah", "name", "number", "text", "date").forEach { edit.remove("${id}_$it") }
        edit.apply()
    }

    companion object {
        val fallback = Ayah("1-1", 1, "الفاتحة", 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ")
    }
}
