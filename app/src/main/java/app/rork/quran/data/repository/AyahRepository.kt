package app.rork.quran.data.repository

import android.content.Context
import app.rork.quran.data.local.dao.AyahDao
import app.rork.quran.data.local.entity.AyahEntity
import app.rork.quran.data.local.entity.ReadVerseEntity
import app.rork.quran.domain.model.Ayah
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AyahRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ayahDao: AyahDao,
    private val gson: Gson,
) {
    private val seedMutex = Mutex()
    private var seeded = false

    /** Seed DB from assets on first launch — safe to call multiple times */
    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (seeded) return@withContext
        seedMutex.withLock {
            if (seeded) return@withLock
            if (ayahDao.count() == 0) {
                val json = context.assets.open("ayahs.json").bufferedReader().readText()
                val type = object : TypeToken<List<AyahEntity>>() {}.type
                val entities: List<AyahEntity> = gson.fromJson(json, type)
                ayahDao.insertAll(entities)
            }
            seeded = true
        }
    }

    suspend fun getRandomAyah(): Ayah = withContext(Dispatchers.IO) {
        ensureSeeded() // always ensure seeded before querying
        val total = ayahDao.count()
        val readCount = ayahDao.readCount()
        if (readCount >= total) ayahDao.resetReadVerses()
        val entity = ayahDao.getRandomUnreadAyah() ?: ayahDao.getRandomAyah()
        ayahDao.markAsRead(ReadVerseEntity(entity.id))
        entity.toDomain()
    }

    suspend fun getAyahById(id: String): Ayah = withContext(Dispatchers.IO) {
        ensureSeeded()
        (ayahDao.getAyahById(id) ?: ayahDao.getRandomAyah()).toDomain()
    }

    /** Get N random ayahs for scheduling notifications (no read tracking) */
    suspend fun getRandomAyahsForNotifications(count: Int): List<Ayah> =
        withContext(Dispatchers.IO) {
            ensureSeeded()
            ayahDao.getRandomAyahs(count).map { it.toDomain() }
        }

    private fun AyahEntity.toDomain() = Ayah(
        id = id,
        surahNumber = surahNumber,
        surahNameArabic = surahNameArabic,
        ayahNumber = ayahNumber,
        textArabic = textArabic,
    )
}