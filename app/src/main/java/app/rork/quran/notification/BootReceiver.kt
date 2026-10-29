package app.rork.quran.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserManager
import android.util.Log
import java.util.concurrent.Executors

/** No Hilt, Room, DataStore or WorkManager is needed to restore or deliver an alarm. */
class BootReceiver : BroadcastReceiver() {
    companion object { private val executor = Executors.newSingleThreadExecutor() }

    override fun onReceive(context: Context, intent: Intent) {
        val actions = setOf(Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            "android.intent.action.QUICKBOOT_POWERON", "com.htc.intent.action.QUICKBOOT_POWERON")
        if (intent.action !in actions) return
        val result = goAsync()
        executor.execute {
            val store = ReminderAlarmStore(context)
            try {
                if (intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED || intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    store.recordBoot(intent.action!!)
                }
                store.restore(recalculate = intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED)
                if ((context.getSystemService(Context.USER_SERVICE) as UserManager).isUserUnlocked) {
                    ReminderRecoveryWorker.ensurePeriodicRecovery(context)
                }
            } catch (error: Exception) {
                store.recordError(error)
                Log.e("BootReceiver", "Reminder recovery failed", error)
            } finally { result.finish() }
        }
    }
}
