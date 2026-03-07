package app.rork.quran.ui.screen.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.StatsRepository
import app.rork.quran.domain.model.Ayah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val ayahRepository: AyahRepository,
) : ViewModel() {

    // Fix 6: Use explicit MutableStateFlow instead of chaining .map{} with suspend calls
    // inside a Flow operator, which caused crashes on rapid navigation.
    private val _bookmarkedAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val bookmarkedAyahs: StateFlow<List<Ayah>> = _bookmarkedAyahs.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            statsRepository.observeStats().collectLatest { stats ->
                val ayahs = stats.bookmarkedIds.mapNotNull { id ->
                    runCatching { ayahRepository.getAyahById(id) }.getOrNull()
                }
                _bookmarkedAyahs.value = ayahs
            }
        }
    }

    fun removeBookmark(ayahId: String) {
        viewModelScope.launch { statsRepository.toggleBookmark(ayahId) }
    }
}
