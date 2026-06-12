package dev.jdtech.jellyfin.presentation.curated

import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MoviesPage
import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.PlaybackProgress
import dev.jdtech.jellyfin.curated.repository.CuratedRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedHistoryLoaderTest {
    @Test
    fun loadSortsProgressByUpdatedAtDescendingAndSkipsFailedDetails() = runBlocking {
        val repository =
            FakeCuratedRepository(
                progress =
                    listOf(
                        playbackProgress(movieId = "movie-old", updatedAt = "2026-06-07T12:00:00Z"),
                        playbackProgress(movieId = "movie-missing", updatedAt = "2026-06-09T12:00:00Z"),
                        playbackProgress(movieId = "movie-new", updatedAt = "2026-06-10T12:00:00Z"),
                    ),
                movies =
                    mapOf(
                        "movie-old" to movieDetail(id = "movie-old", title = "Old Movie"),
                        "movie-new" to movieDetail(id = "movie-new", title = "New Movie"),
                    ),
            )

        val items = CuratedHistoryLoader(repository).load()

        assertEquals(listOf("movie-new", "movie-old"), items.map { it.movie.id })
        assertEquals(listOf("2026-06-10T12:00:00Z", "2026-06-07T12:00:00Z"), items.map { it.progress.updatedAt })
    }

    private class FakeCuratedRepository(
        private val progress: List<PlaybackProgress>,
        private val movies: Map<String, MovieDetail>,
    ) : CuratedRepository {
        override suspend fun getMovies(
            limit: Int,
            offset: Int,
            query: String?,
            actor: String?,
            studio: String?,
            mode: String?,
        ): MoviesPage = error("Not used")

        override suspend fun getMovie(movieId: String): MovieDetail =
            movies[movieId] ?: error("Missing movie $movieId")

        override suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor =
            error("Not used")

        override suspend fun getPlaybackProgress(): List<PlaybackProgress> = progress
    }

    private fun playbackProgress(movieId: String, updatedAt: String): PlaybackProgress =
        PlaybackProgress(
            movieId = movieId,
            positionSec = 120.0,
            durationSec = 600.0,
            updatedAt = updatedAt,
        )

    private fun movieDetail(id: String, title: String): MovieDetail =
        MovieDetail(
            id = id,
            title = title,
            code = "ABC-001",
            studio = "Studio",
            actors = emptyList(),
            tags = emptyList(),
            userTags = emptyList(),
            runtimeMinutes = 120,
            rating = 0.0,
            isFavorite = false,
            addedAt = "2026-06-07T12:00:00Z",
            location = "",
            resolution = "1080p",
            year = 2026,
            releaseDate = null,
            coverUrl = null,
            thumbUrl = null,
            trashedAt = null,
            summary = "",
            previewImages = emptyList(),
            previewVideoUrl = null,
            metadataRating = 0.0,
            userRating = null,
            actorAvatarUrls = emptyMap(),
        )
}
