package app.rork.quran.passages

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.data.repository.PassageCatalog
import app.rork.quran.data.repository.TafseerRepository
import app.rork.quran.ui.tafseer.TafseerViewModel
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PassageTafseerTest {
    @Test fun passageLoadsAnExplanationForEveryVerseInOrder() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val passage = PassageCatalog(context).resolve("107-4")
        val directory = java.io.File(context.filesDir, "tafseer_muyassar_qurancom").apply { mkdirs() }
        val backups = passage.verses.orEmpty().associate { verse ->
            val file = java.io.File(directory, "107_${verse.number}.json")
            file to file.takeIf { it.exists() }?.readText()
        }
        try {
            passage.verses.orEmpty().forEach { verse ->
                java.io.File(directory, "107_${verse.number}.json").writeText(
                    JSONObject().put("tafsir", JSONObject().put("slug", "ar-tafsir-muyassar")
                        .put("verses", JSONObject().put("107:${verse.number}", JSONObject()))
                        .put("text", "شرح تجريبي ${verse.number}")).toString())
            }
            val viewModel = TafseerViewModel(TafseerRepository(context))
            instrumentation.runOnMainSync { viewModel.load(passage) }
            val deadline = android.os.SystemClock.elapsedRealtime() + 10_000
            while (viewModel.state.value.loading && android.os.SystemClock.elapsedRealtime() < deadline) Thread.sleep(50)
            assertFalse(viewModel.state.value.failed)
            assertEquals(passage.verses.orEmpty().joinToString("\n\n") {
                "الآية ${it.number}\nشرح تجريبي ${it.number}"
            }, viewModel.state.value.text)
        } finally {
            backups.forEach { (file, text) -> if (text == null) file.delete() else file.writeText(text) }
        }
    }
}
