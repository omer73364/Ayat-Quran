package app.rork.quran.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var ayahRepository: AyahRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val bootActions = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_LOCKED_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in bootActions) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val settings = settingsRepository.observeSettings().first()
                if (settings.reminderEnabled) {
                    ayahRepository.ensureSeeded()
                    val ayahs = ayahRepository.getRandomAyahsForNotifications(70)
                    alarmScheduler.schedule(settings, ayahs)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
