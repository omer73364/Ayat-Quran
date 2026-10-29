package app.rork.quran.notification

import android.content.Context
import app.rork.quran.data.repository.SettingsRepository
import app.rork.quran.domain.model.Ayah
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsRepository: SettingsRepository,
) {
    companion object {
        const val ACTION_SHOW_NOTIFICATION = "app.rork.quran.SHOW_NOTIFICATION"
        const val EXTRA_AYAH_JSON = "ayah_json"
        const val EXTRA_ALARM_ID = "alarm_id"
    }

    private val store = ReminderAlarmStore(context)
    fun canScheduleExact() = store.canScheduleExact()
    fun nextReminderTime() = store.nextReminderTime()
    fun recoveryStatus() = store.recoveryStatus()

    suspend fun cancelAll() = withContext(Dispatchers.IO) {
        settingsRepository.withCurrentSettings { if (!it.reminderEnabled) store.disable() }
    }

    suspend fun schedule(ayahs: List<Ayah>, force: Boolean = true) = withContext(Dispatchers.IO) {
        settingsRepository.withCurrentSettings { store.replace(it, ayahs, force) }
    }
}
