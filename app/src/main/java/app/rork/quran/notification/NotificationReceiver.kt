package app.rork.quran.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.rork.quran.data.repository.StatsRepository
import app.rork.quran.domain.model.Ayah
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var statsRepository: StatsRepository
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

            // User tapped "✔ قرأت" button → mark as read silently + dismiss
            NotificationHelper.ACTION_READ -> {
                val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val ayahId = intent.getStringExtra(NotificationHelper.EXTRA_AYAH_ID)
                            ?: return@launch
                        val date = java.time.LocalDate.now().toString()
                        statsRepository.markAsRead("$date:$ayahId")
                        notificationHelper.dismissNotification(notificationId)

                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        android.util.Log.e("NotificationReceiver", "Could not record reading", error)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

}
