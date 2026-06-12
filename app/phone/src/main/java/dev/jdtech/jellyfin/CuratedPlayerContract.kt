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

internal fun PlaybackDescriptor.curatedStartPositionMs(): Long {
    val startSeconds = startPositionSec ?: 0.0
    return (startSeconds * 1000.0).roundToLong().coerceAtLeast(0L)
}
