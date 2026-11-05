package app.rork.quran.ui.tafseer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rork.quran.data.repository.TafseerRepository
import app.rork.quran.domain.model.Ayah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

data class TafseerState(val text: String? = null, val loading: Boolean = true, val failed: Boolean = false)

@HiltViewModel
class TafseerViewModel @Inject constructor(private val repository: TafseerRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(TafseerState())
    val state = mutableState.asStateFlow()
    private var job: kotlinx.coroutines.Job? = null

    fun load(ayah: Ayah) {
        if (job?.isActive == true || mutableState.value.text != null) return
        job = viewModelScope.launch {
            mutableState.value = TafseerState()
            try {
                val text = if (ayah.verses.orEmpty().size <= 1) repository.get(ayah) else {
                    val permits = kotlinx.coroutines.sync.Semaphore(3)
                    kotlinx.coroutines.coroutineScope {
                        ayah.verses.orEmpty().map { verse ->
                            async {
                                permits.withPermit {
                                    "الآية ${verse.number}\n" + repository.get(ayah.copy(
                                        id = verse.id, ayahNumber = verse.number, textArabic = verse.text, verses = null))
                                }
                            }
                        }.awaitAll().joinToString("\n\n")
                    }
                }
                mutableState.value = TafseerState(text = text, loading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = TafseerState(loading = false, failed = true)
            }
        }
    }
}
