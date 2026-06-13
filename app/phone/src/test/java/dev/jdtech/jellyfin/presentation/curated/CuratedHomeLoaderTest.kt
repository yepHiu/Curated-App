package dev.jdtech.jellyfin.presentation.curated

import dev.jdtech.jellyfin.curated.api.ActorProfile
import dev.jdtech.jellyfin.curated.api.ActorsPage
import dev.jdtech.jellyfin.curated.api.HomepageDailyRecommendations
import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MoviesPage
import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.PlaybackProgress
import dev.jdtech.jellyfin.curated.repository.CuratedRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedHomeLoaderTest {
    @Test
    fun loadKeepsRecommendationOrderDeduplicatesDetailRequestsAndSkipsMissingMovies() =
        runBlocking {
            val repository =
                FakeCuratedRepository(
                    recommendations =
                        HomepageDailyRecommendations(
                            dateUtc = "2026-06-13",
                            generatedAt = "2026-06-13T00:00:00Z",
                            generationVersion = "v1",
                            heroMovieIds = listOf("hero-1", "shared"),
                            recommendationMovieIds = listOf("shared", "rec-1", "missing"),
                        ),
                    movies =
                        mapOf(
                            "hero-1" to movieDetail("hero-1"),
                            "shared" to movieDetail("shared"),
                            "rec-1" to movieDetail("rec-1"),
                        ),
                )

            val content = CuratedHomeLoader(repository).load()

            assertEquals("2026-06-13", content.dateUtc)
            assertEquals(listOf("hero-1", "shared"), content.heroMovies.map { it.id })
            assertEquals(listOf("shared", "rec-1"), content.todayRecommendations.map { it.id })
            assertEquals(listOf("hero-1", "shared", "rec-1", "missing"), repository.movieRequests)
        }

    @Test
    fun loadReturnsEmptySectionsWhenSnapshotHasNoMovieIds() = runBlocking {
        val repository =
            FakeCuratedRepository(
                recommendations =
                    HomepageDailyRecommendations(
                        dateUtc = "2026-06-13",
                        generatedAt = "2026-06-13T00:00:00Z",
                        generationVersion = "v1",
                        heroMovieIds = emptyList(),
                        recommendationMovieIds = emptyList(),
                    ),
                movies = emptyMap(),
            )

        val content = CuratedHomeLoader(repository).load()

        assertEquals(emptyList<MovieDetail>(), content.heroMovies)
        assertEquals(emptyList<MovieDetail>(), content.todayRecommendations)
        assertEquals(emptyList<String>(), repository.movieRequests)
    }

    private class FakeCuratedRepository(
        private val recommendations: HomepageDailyRecommendations,
        private val movies: Map<String, MovieDetail>,
    ) : CuratedRepository {
        val movieRequests = mutableListOf<String>()

        override suspend fun getHomepageRecommendations(): HomepageDailyRecommendations =
            recommendations

        override suspend fun getMovies(
            limit: Int,
            offset: Int,
            query: String?,
            actor: String?,
            studio: String?,
            mode: String?,
        ): MoviesPage = error("Not used")

        override suspend fun getActors(
            limit: Int,
            offset: Int,
            query: String?,
            actorTag: String?,
            sort: String?,
        ): ActorsPage = error("Not used")

        override suspend fun getActorProfile(name: String): ActorProfile = error("Not used")

        override suspend fun getMovie(movieId: String): MovieDetail {
            movieRequests += movieId
            return movies[movieId] ?: error("Missing movie $movieId")
        }

        override suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor =
            error("Not used")

        override suspend fun getPlaybackProgress(): List<PlaybackProgress> = error("Not used")

        override suspend fun updatePlaybackProgress(
            movieId: String,
            positionSec: Double,
            durationSec: Double?,
        ) = error("Not used")
    }

    private fun movieDetail(id: String): MovieDetail =
        MovieDetail(
            id = id,
            title = "Movie $id",
            code = "ABC-001",
            studio = "Studio",
            actors = emptyList(),
            tags = emptyList(),
            userTags = emptyList(),
            runtimeMinutes = 120,
            rating = 0.0,
            isFavorite = false,
            addedAt = "2026-06-13T12:00:00Z",
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
