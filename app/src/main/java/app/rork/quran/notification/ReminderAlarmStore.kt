package app.rork.quran.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.Keep
import app.rork.quran.domain.model.Ayah
import app.rork.quran.domain.model.UserSettings
import com.google.gson.Gson

@Keep
data class PlannedReminder(val id: Int, val time: Long, val ayah: Ayah)

@Keep
data class ReminderSnapshot(val settings: UserSettings, val ayahs: List<Ayah>, val alarms: List<PlannedReminder>)

/** Only the public verse payloads and reminder plan live here. No database or reading history. */
class ReminderAlarmStore(context: Context) {
    private val context = context.applicationContext
    private val prefs = context.createDeviceProtectedStorageContext()
        .getSharedPreferences("boot_reminders", Context.MODE_PRIVATE)
    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val gson = Gson()

    companion object { private val lock = Any() }

    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    private fun bootCount() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    private fun read(): ReminderSnapshot? = prefs.getString("snapshot", null)?.let {
        gson.fromJson(it, ReminderSnapshot::class.java)
    }

    fun replace(settings: UserSettings, ayahs: List<Ayah>, force: Boolean = true) = synchronized(lock) {
        if (!settings.reminderEnabled) { disable(); return@synchronized }
        require(ayahs.isNotEmpty()) { "No verses available for reminders" }
        val now = System.currentTimeMillis()
        val old = read()
        if (!force && old != null && sameSchedule(old.settings, settings) &&
            old.alarms.any { it.time > now + 2 * 24 * 60 * 60 * 1000L } &&
            prefs.getInt("scheduled_boot", -2) == bootCount()) return@synchronized
        val alarms = ReminderTimes.create(settings, now).mapIndexed { index, time ->
            PlannedReminder(index, time, ayahs[index % ayahs.size])
        }
        // Persist BEFORE registering: a reboot never depends on an unfinished Room/DataStore read.
        check(prefs.edit().putString("snapshot", gson.toJson(ReminderSnapshot(settings, ayahs, alarms))).commit())
        cancelPendingAlarms()
        register(alarms)
    }

    fun restore(recalculate: Boolean = false) = synchronized(lock) {
        val snapshot = read() ?: return@synchronized
        if (!snapshot.settings.reminderEnabled) return@synchronized
        val future = snapshot.alarms.filter { it.time > System.currentTimeMillis() }
        if (recalculate || future.isEmpty()) replace(snapshot.settings, snapshot.ayahs)
        else register(future)
    }

    fun disable() = synchronized(lock) {
        check(prefs.edit().remove("snapshot").remove("scheduled_at").remove("scheduled_boot").commit())
        cancelPendingAlarms()
    }

    private fun sameSchedule(a: UserSettings, b: UserSettings) =
        a.reminderEnabled == b.reminderEnabled && a.remindersPerDay == b.remindersPerDay &&
            a.reminderFromHour == b.reminderFromHour && a.reminderFromMinute == b.reminderFromMinute &&
            a.reminderToHour == b.reminderToHour && a.reminderToMinute == b.reminderToMinute

    private fun alarmIntent(id: Int, time: Long) = Intent(context, ReminderAlarmReceiver::class.java).apply {
        action = AlarmScheduler.ACTION_SHOW_NOTIFICATION
        putExtra(AlarmScheduler.EXTRA_ALARM_ID, id)
        putExtra("trigger_at", time)
    }

    private fun register(alarms: List<PlannedReminder>) {
        for (alarm in alarms) {
            val pending = PendingIntent.getBroadcast(context, alarm.id, alarmIntent(alarm.id, alarm.time),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                if (canScheduleExact()) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.time, pending)
                else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.time, pending)
            } catch (_: SecurityException) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.time, pending)
            }
        }
        check(prefs.edit().putLong("scheduled_at", System.currentTimeMillis())
            .putInt("scheduled_boot", bootCount()).remove("error").commit())
    }

    private fun cancelPendingAlarms() {
        for (id in 0 until 70) {
            // Cancel the old receiver's alarms as well when migrating an installed app.
            for (receiver in listOf(ReminderAlarmReceiver::class.java, NotificationReceiver::class.java)) {
                val intent = Intent(context, receiver).setAction(AlarmScheduler.ACTION_SHOW_NOTIFICATION)
                val pending = PendingIntent.getBroadcast(context, id, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: continue
                manager.cancel(pending)
                pending.cancel()
            }
        }
    }

    fun deliveryAyah(id: Int, time: Long): Ayah? = synchronized(lock) {
        read()?.takeIf { it.settings.reminderEnabled }?.alarms
            ?.firstOrNull { it.id == id && it.time == time }?.ayah
    }

    fun nextReminderTime(): Long? = synchronized(lock) {
        if (prefs.getInt("scheduled_boot", -2) != bootCount()) return@synchronized null
        read()?.alarms?.filter { it.time > System.currentTimeMillis() }?.minOfOrNull { it.time }
    }

    fun recordBoot(action: String) {
        prefs.edit().putLong("boot_received_at", System.currentTimeMillis())
            .putInt("received_boot", bootCount()).putString("boot_action", action).commit()
    }

    fun recordError(error: Throwable) { prefs.edit().putString("error", error.javaClass.simpleName).commit() }

    fun recoveryStatus(): String = when {
        prefs.getInt("received_boot", -2) != bootCount() -> "لم تُرصد إشارة إعادة التشغيل الحالية. تحقّق من التشغيل التلقائي."
        prefs.contains("error") -> "وصلت إشارة التشغيل، لكن تعذّرت الاستعادة. أعد جدولة التذكيرات."
        prefs.getInt("scheduled_boot", -2) == bootCount() -> "تمت استعادة التذكيرات بعد تشغيل الهاتف"
        else -> "وصلت إشارة التشغيل؛ فعّل التذكيرات لإعداد المواعيد"
    }
}
