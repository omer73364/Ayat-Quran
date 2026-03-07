package app.rork.quran.domain.model

data class ReadingStats(
    val totalRead: Int = 0,
    val streak: Int = 0,
    val lastReadDate: String = "",
    val dailyHistory: List<DailyRead> = emptyList(),
    val bookmarkedIds: List<String> = emptyList(),
)
