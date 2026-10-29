import app.rork.quran.domain.model.UserSettings
import app.rork.quran.domain.model.searchKey
import app.rork.quran.notification.ReminderTimes
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone
import kotlin.random.Random

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Aden"))
    check("إِنَّ ٱللَّهَ".searchKey() == "ان الله")
    check("سُورَةُ الْبَقَرَةِ".searchKey().contains("البقرة".searchKey()))
    check("الرَّحْمَٰنِ".searchKey().contains("الرحمن".searchKey()))
    check("  عَلَىـ  ".searchKey() == "علي")
    val zone = ZoneId.systemDefault()
    val now = LocalDateTime.of(2026, 10, 22, 12, 0).atZone(zone).toInstant().toEpochMilli()
    val settings = UserSettings(reminderEnabled = true, remindersPerDay = 4)
    val times = ReminderTimes.create(settings, now, Random(42))
    check(times.all { it > now })
    check(times == times.distinct().sorted())
    check(times.groupBy { java.time.Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.values.all { it.size <= 4 })
    check(times == ReminderTimes.create(settings.copy(dailyGoal = 50), now, Random(42)))
    check(ReminderTimes.create(settings.copy(reminderEnabled = false), now).isEmpty())
    val midnight = LocalDateTime.of(2026, 10, 22, 0, 0).atZone(zone).toInstant().toEpochMilli()
    val overnight = ReminderTimes.create(settings.copy(reminderFromHour = 22, reminderToHour = 6), midnight, Random(42))
    check(overnight.size == 28)
    check(overnight.all { java.time.Instant.ofEpochMilli(it).atZone(zone).hour.let { hour -> hour >= 22 || hour < 6 } })
    check(java.time.Instant.ofEpochMilli(overnight.last()).atZone(zone).toLocalDate().toString() == "2026-10-29")
    println("PASS: Arabic search, future slots, uniqueness, independent reminder frequency, disabled reminders, overnight dates")
}
