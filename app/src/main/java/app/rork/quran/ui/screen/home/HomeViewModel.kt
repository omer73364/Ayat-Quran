package app.rork.quran.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.SettingsRepository
import app.rork.quran.data.repository.StatsRepository
import app.rork.quran.domain.model.Ayah
import app.rork.quran.domain.model.DailyRead
import app.rork.quran.domain.model.ReadingStats
import app.rork.quran.domain.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val currentAyah: Ayah? = null,
    val stats: ReadingStats = ReadingStats(),
    val settings: UserSettings = UserSettings(),
    val weeklyHistory: List<DailyRead> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ayahRepository: AyahRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _currentAyah = MutableStateFlow<Ayah?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        _currentAyah,
        statsRepository.observeStats(),
        settingsRepository.observeSettings(),
    ) { ayah, stats, settings ->
        HomeUiState(
            currentAyah    = ayah,
            stats          = stats,
            settings       = settings,
            weeklyHistory  = buildWeeklyHistory(stats.dailyHistory),
            isLoading      = ayah == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        loadNextAyah()
    }

    fun loadNextAyah(ayahId: String? = null) {
        viewModelScope.launch {
            val ayah = if (ayahId != null) ayahRepository.getAyahById(ayahId)
                       else ayahRepository.getRandomAyah()
            _currentAyah.value = ayah
        }
    }

    fun markAsRead() {
        viewModelScope.launch { statsRepository.markAsRead() }
    }

    fun loadNextAndMarkRead(ayahId: String? = null) {
        viewModelScope.launch {
            statsRepository.markAsRead()
            val next = if (ayahId != null) ayahRepository.getAyahById(ayahId)
                       else ayahRepository.getRandomAyah()
            _currentAyah.value = next
        }
    }

    fun toggleBookmark(ayahId: String) {
        viewModelScope.launch { statsRepository.toggleBookmark(ayahId) }
    }

    fun isBookmarked(ayahId: String): Boolean =
        uiState.value.stats.bookmarkedIds.contains(ayahId)

    private fun buildWeeklyHistory(history: List<DailyRead>): List<DailyRead> {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        return (6 downTo 0).map { daysAgo ->
            val date = LocalDate.now().minusDays(daysAgo.toLong()).format(fmt)
            history.find { it.date == date } ?: DailyRead(date, 0)
        }
    }
}
