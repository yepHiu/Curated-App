package dev.jdtech.jellyfin

import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import kotlin.math.roundToLong

object CuratedPlayerContract {
    const val EXTRA_MOVIE_ID = "curatedMovieId"
    const val EXTRA_TITLE = "curatedTitle"
}

internal fun curatedPlayerExtras(movieId: String, title: String?): Map<String, String?> =
    mapOf(
        CuratedPlayerContract.EXTRA_MOVIE_ID to movieId,
        CuratedPlayerContract.EXTRA_TITLE to title,
    )

internal fun curatedHistoryPlayerExtras(movieId: String, title: String): Map<String, String?> =
    curatedPlayerExtras(movieId = movieId, title = title)

internal fun PlaybackDescriptor.curatedStartPositionMs(): Long {
    val startSeconds = resumePositionSec ?: startPositionSec ?: 0.0
    return (startSeconds * 1000.0).roundToLong().coerceAtLeast(0L)
}

internal data class CuratedPlaybackProgressUpdate(
    val movieId: String,
    val positionSec: Double,
    val durationSec: Double?,
)

internal fun curatedPlaybackProgressUpdate(
    movieId: String?,
    positionMs: Long,
    durationMs: Long,
): CuratedPlaybackProgressUpdate? {
    val normalizedMovieId = movieId?.takeIf { it.isNotBlank() } ?: return null
    if (positionMs <= 0L) return null
    return CuratedPlaybackProgressUpdate(
        movieId = normalizedMovieId,
        positionSec = positionMs / 1000.0,
        durationSec = durationMs.takeIf { it > 0L }?.let { it / 1000.0 },
    )
}
