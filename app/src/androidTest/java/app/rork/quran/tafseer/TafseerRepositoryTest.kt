package app.rork.quran.tafseer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.data.repository.TafseerRepository
import app.rork.quran.domain.model.Ayah
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TafseerRepositoryTest {
    private val repository = TafseerRepository(InstrumentationRegistry.getInstrumentation().targetContext)
    private val ayah = Ayah("2-255", 2, "البقرة", 255, "")
    private fun response(slug: String = "ar-tafsir-muyassar", key: String = "2:255", text: String = "<p>تفسير <b>الآية</b> &amp; معناها</p>") =
        JSONObject().put("tafsir", JSONObject().put("slug", slug)
            .put("verses", JSONObject().put(key, JSONObject())).put("text", text)).toString()

    @Test fun parsesArabicAndRemovesHtmlFormatting() {
        assertEquals("تفسير الآية & معناها", repository.parse(response(), ayah))
    }

    @Test fun rejectsTheWrongVerseOrTafseerAndEmptyContent() {
        listOf(response(key = "2:254"), response(slug = "ar-tafsir-ibn-kathir"), response(text = "<p> </p>"))
            .forEach { invalid -> assertTrue(runCatching { repository.parse(invalid, ayah) }.isFailure) }
    }

    @Test fun readsPreviouslyCachedTafseerWithoutARequest() = kotlinx.coroutines.runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = java.io.File(context.filesDir, "tafseer_muyassar_qurancom").apply { mkdirs() }
        val cache = java.io.File(directory, "2_255.json")
        val previous = cache.takeIf { it.exists() }?.readText()
        try {
            cache.writeText(response(text = "تفسير محفوظ للاختبار"))
            assertEquals("تفسير محفوظ للاختبار", repository.get(ayah))
        } finally {
            if (previous == null) cache.delete() else cache.writeText(previous)
        }
    }
}
