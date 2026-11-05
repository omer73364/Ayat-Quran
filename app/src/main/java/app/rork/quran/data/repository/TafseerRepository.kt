package app.rork.quran.data.repository

import android.content.Context
import app.rork.quran.domain.model.Ayah
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TafseerRepository @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun get(ayah: Ayah): String = withContext(Dispatchers.IO) {
        require(ayah.surahNumber in 1..114 && ayah.ayahNumber in 1..286)
        val directory = File(context.filesDir, "tafseer_muyassar_qurancom").apply { mkdirs() }
        val cache = File(directory, "${ayah.surahNumber}_${ayah.ayahNumber}.json")
        if (cache.exists()) {
            runCatching { parse(cache.readText(), ayah) }.getOrNull()?.let { return@withContext it }
        }
        val connection = URL("https://api.quran.com/api/v4/tafsirs/ar-tafsir-muyassar/by_ayah/${ayah.surahNumber}:${ayah.ayahNumber}")
            .openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == 200) { "Tafseer request failed" }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var count = input.read(buffer)
                while (count != -1) {
                    check(output.size() + count <= 256 * 1024) { "Unexpected response size" }
                    output.write(buffer, 0, count)
                    count = input.read(buffer)
                }
                output.toByteArray()
            }
            check(bytes.size <= 256 * 1024) { "Unexpected response size" }
            val json = bytes.toString(Charsets.UTF_8)
            val text = parse(json, ayah)
            // A storage failure should not hide a successfully fetched explanation.
            runCatching { cache.writeText(json) }
            text
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(json: String, ayah: Ayah): String {
        val response = JSONObject(json).getJSONObject("tafsir")
        check(response.getString("slug") == "ar-tafsir-muyassar")
        check(response.getJSONObject("verses").has("${ayah.surahNumber}:${ayah.ayahNumber}"))
        // The API contains formatting tags; render their readable text, never raw HTML.
        return androidx.core.text.HtmlCompat.fromHtml(response.getString("text"),
            androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            .also { check(it.isNotEmpty()) }
    }
}
