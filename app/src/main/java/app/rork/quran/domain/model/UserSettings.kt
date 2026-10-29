package app.rork.quran.domain.model

data class UserSettings(
    val reminderEnabled: Boolean = false,
    val reminderFromHour: Int = 6,
    val reminderFromMinute: Int = 0,
    val reminderToHour: Int = 22,
    val reminderToMinute: Int = 0,
    val dailyGoal: Int = 5,
    val remindersPerDay: Int = 5,
    val arabicFontSize: Int = 28,
    val darkMode: Boolean = false,
)
