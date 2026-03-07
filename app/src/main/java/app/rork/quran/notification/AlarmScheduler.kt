package app.rork.quran.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.rork.quran.domain.model.Ayah
import app.rork.quran.domain.model.UserSettings
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) {
    companion object {
        const val ACTION_SHOW_NOTIFICATION = "app.rork.quran.SHOW_NOTIFICATION"
        const val EXTRA_AYAH_JSON = "ayah_json"
        const val EXTRA_ALARM_ID = "alarm_id"

        // We store up to 70 alarms (7 days × 10 max per day)
        private const val MAX_ALARMS = 70
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExact(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    fun cancelAll() {
        for (id in 0 until MAX_ALARMS) {
            val intent = buildAlarmIntent(id, null)
            val pi = PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) ?: continue
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    fun schedule(settings: UserSettings, ayahs: List<Ayah>) {
        cancelAll()
        if (!settings.reminderEnabled || ayahs.isEmpty()) return

        val fromMinutes = settings.reminderFromHour * 60 + settings.reminderFromMinute
        var toMinutes = settings.reminderToHour * 60 + settings.reminderToMinute
        if (toMinutes <= fromMinutes) toMinutes += 24 * 60

        val count = min(settings.dailyGoal, 10)
        val range = toMinutes - fromMinutes
        if (range <= 0 || count <= 0) return

        var alarmId = 0
        val ayahPool = ayahs.toMutableList()

        for (day in 0 until 7) {
            val slots = generateSpacedSlots(fromMinutes, range, count)
            for (slotMinutes in slots) {
                if (alarmId >= MAX_ALARMS) break
                val totalMinutes = slotMinutes % (24 * 60)
                val hour = totalMinutes / 60
                val minute = totalMinutes % 60

                val triggerTime = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, day)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }.timeInMillis

                val ayah = ayahPool[alarmId % ayahPool.size]
                val intent = buildAlarmIntent(alarmId, ayah)
                val pi = PendingIntent.getBroadcast(
                    context, alarmId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canScheduleExact()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTime, pi),
                        pi,
                    )
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pi,
                    )
                } else {
                    // Fallback: inexact (still works, may be slightly delayed)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pi,
                    )
                }
                alarmId++
            }
        }
    }

    private fun buildAlarmIntent(alarmId: Int, ayah: Ayah?): Intent =
        Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SHOW_NOTIFICATION
            putExtra(EXTRA_ALARM_ID, alarmId)
            ayah?.let { putExtra(EXTRA_AYAH_JSON, gson.toJson(it)) }
        }

    /**
     * Generates `count` time offsets (absolute minutes since midnight)
     * evenly spaced across [fromMinutes, fromMinutes+range] with random jitter.
     */
    private fun generateSpacedSlots(fromMinutes: Int, range: Int, count: Int): List<Int> {
        val slotSize = range.toDouble() / count
        return (0 until count).mapNotNull { i ->
            val subStart = (i * slotSize).toInt()
            val subEnd = ((i + 1) * slotSize).toInt() - 1
            if (subEnd <= subStart) return@mapNotNull fromMinutes + subStart
            fromMinutes + subStart + (Math.random() * (subEnd - subStart)).toInt()
        }
    }
}
