package app.rork.quran.notification

import app.rork.quran.domain.model.UserSettings
import java.util.Calendar
import kotlin.random.Random

/** Pure calculation shared by scheduling and regression checks. */
internal object ReminderTimes {
    fun create(settings: UserSettings, now: Long, random: Random = Random.Default): List<Long> {
        if (!settings.reminderEnabled) return emptyList()
        val from = settings.reminderFromHour * 60 + settings.reminderFromMinute
        var to = settings.reminderToHour * 60 + settings.reminderToMinute
        if (to <= from) to += 24 * 60
        val count = settings.remindersPerDay.coerceIn(1, 10)
        val slotSize = (to - from).toDouble() / count
        return (0 until 7).flatMap { day ->
            (0 until count).map { index ->
                val start = (index * slotSize).toInt()
                val end = ((index + 1) * slotSize).toInt()
                val minutes = from + start + if (end > start) random.nextInt(end - start) else 0
                Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_YEAR, day + minutes / (24 * 60))
                    set(Calendar.HOUR_OF_DAY, minutes % (24 * 60) / 60)
                    set(Calendar.MINUTE, minutes % 60)
                }.timeInMillis
            }
        }.filter { it > now }.distinct().sorted()
    }
}
