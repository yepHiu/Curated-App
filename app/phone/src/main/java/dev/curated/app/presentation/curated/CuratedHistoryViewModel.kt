package dev.curated.app.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.curated.repository.CuratedRepository
import dev.curated.app.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@HiltViewModel
class CuratedHistoryViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedHistoryState())
    val state = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val repository = repositoryFactory.createForCurrentServer()
                val items = CuratedHistoryLoader(repository).load()
                _state.value = CuratedHistoryState(isLoading = false, items = items)
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Unable to load watching history",
                    )
            }
        }
    }
}

data class CuratedHistoryState(
    val isLoading: Boolean = true,
    val items: List<CuratedHistoryItem> = emptyList(),
    val errorMessage: String? = null,
)

data class CuratedHistoryItem(
    val movie: MovieDetail,
    val progress: PlaybackProgress,
)

internal class CuratedHistoryLoader(
    private val repository: CuratedRepository,
    private val maxConcurrentMovieRequests: Int = 6,
) {
    suspend fun load(): List<CuratedHistoryItem> = coroutineScope {
        val movieRequestSemaphore = Semaphore(maxConcurrentMovieRequests)
        repository
            .getPlaybackProgress()
            .sortedByDescending { it.updatedAt }
            .map { progress ->
                async {
                    movieRequestSemaphore.withPermit {
                        runCatching {
                                CuratedHistoryItem(
                                    movie = repository.getMovie(progress.movieId),
                                    progress = progress,
                                )
                            }
                            .getOrNull()
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
    }
}

internal fun curatedHistoryProgressFraction(positionSec: Double, durationSec: Double?): Float {
    if (durationSec == null || durationSec <= 0.0) return 0f
    return (positionSec / durationSec).toFloat().coerceIn(0f, 1f)
}

internal fun curatedHistoryProgressPercentText(positionSec: Double, durationSec: Double?): String =
    "${(curatedHistoryProgressFraction(positionSec, durationSec) * 100).roundToInt()}%"

internal fun curatedHistoryProgressTimeText(positionSec: Double, durationSec: Double?): String {
    val position = formatHistorySeconds(positionSec)
    val duration = durationSec?.takeIf { it > 0.0 }?.let(::formatHistorySeconds)
    return if (duration != null) "$position / $duration" else "$position watched"
}

internal fun curatedHistoryImageUrl(thumbUrl: String?, coverUrl: String?): String? =
    thumbUrl ?: coverUrl

private fun formatHistorySeconds(seconds: Double): String {
    val totalSeconds = seconds.coerceAtLeast(0.0).toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val remainingSeconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
    } else {
        "%d:%02d".format(minutes, remainingSeconds)
    }
}
