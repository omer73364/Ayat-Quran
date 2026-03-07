package app.rork.quran.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.SettingsRepository
import app.rork.quran.data.repository.StatsRepository
import app.rork.quran.domain.model.Ayah
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var statsRepository: StatsRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var ayahRepository: AyahRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var gson: Gson

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            // Alarm fired → show the notification
            AlarmScheduler.ACTION_SHOW_NOTIFICATION -> {
                val ayahJson = intent.getStringExtra(AlarmScheduler.EXTRA_AYAH_JSON) ?: return
                val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, 0)
                val ayah = runCatching { gson.fromJson(ayahJson, Ayah::class.java) }
                    .getOrNull() ?: return
                notificationHelper.showAyahNotification(ayah, alarmId)
            }

            // User tapped the notification BODY → mark as read + open app + dismiss
            NotificationHelper.ACTION_TAP -> {
                val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        statsRepository.markAsRead()
                        // rescheduleRollingWindow()
                    } finally {
                        notificationHelper.dismissNotification(notificationId)
                        // Launch the app
                        context.packageManager
                            .getLaunchIntentForPackage(context.packageName)
                            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP }
                            ?.let { context.startActivity(it) }
                        pendingResult.finish()
                    }
                }
            }

            // User tapped "✔ قرأت" button → mark as read silently + reschedule + dismiss
            NotificationHelper.ACTION_READ -> {
                val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        statsRepository.markAsRead()
                        rescheduleRollingWindow()
                    } finally {
                        notificationHelper.dismissNotification(notificationId)
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    /**
     * Fix 5: Each time the user reads an ayah via a notification, we reschedule
     * a fresh 7-day window of alarms. This means notifications never stop —
     * more reads = more future slots scheduled indefinitely.
     */
    private suspend fun rescheduleRollingWindow() {
        val settings = settingsRepository.observeSettings().first()
        if (!settings.reminderEnabled) return
        val ayahs = ayahRepository.getRandomAyahsForNotifications(70)
        alarmScheduler.schedule(settings, ayahs)
    }
}
