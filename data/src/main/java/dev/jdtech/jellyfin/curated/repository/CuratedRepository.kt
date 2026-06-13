package dev.jdtech.jellyfin.curated.repository

import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MoviesPage
import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.PlaybackProgress

interface CuratedRepository {
    suspend fun getMovies(
        limit: Int = 50,
        offset: Int = 0,
        query: String? = null,
        actor: String? = null,
        studio: String? = null,
        mode: String? = null,
    ): MoviesPage

    suspend fun getMovie(movieId: String): MovieDetail

    suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor

    suspend fun getPlaybackProgress(): List<PlaybackProgress>

    suspend fun updatePlaybackProgress(
        movieId: String,
        positionSec: Double,
        durationSec: Double?,
    )
}
