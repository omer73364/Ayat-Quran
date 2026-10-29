package app.rork.quran.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.Executors

/** Alarm delivery can run before first unlock; reading actions remain credential protected. */
class ReminderAlarmReceiver : BroadcastReceiver() {
    companion object { private val executor = Executors.newSingleThreadExecutor() }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_SHOW_NOTIFICATION) return
        val result = goAsync()
        executor.execute {
            val store = ReminderAlarmStore(context)
            try {
                val id = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
                val ayah = store.deliveryAyah(id, intent.getLongExtra("trigger_at", -1L)) ?: return@execute
                NotificationHelper(context).apply { createChannel(); showAyahNotification(ayah, id) }
                // The last delivered alarm replenishes the plan even if the app is never opened.
                store.restore()
            } catch (error: Exception) {
                store.recordError(error)
                Log.e("ReminderAlarmReceiver", "Could not deliver reminder", error)
            } finally { result.finish() }
        }
    }
}
