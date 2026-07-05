package dev.curated.app.presentation.curated

import dev.curated.app.curated.api.ActorProfile
import dev.curated.app.curated.api.ActorsPage
import dev.curated.app.curated.api.HomepageDailyRecommendations
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.api.MovieListItem
import dev.curated.app.curated.api.MoviesPage
import dev.curated.app.curated.api.PlaybackDescriptor
import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.curated.repository.CuratedRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedActorMoviesPagerTest {
    @Test
    fun loadFirstPageRequestsMoviesFilteredByActor() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        MoviesPage(
                            items = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                            total = 5,
                            limit = 2,
                            offset = 0,
                        )
                    )
            )

        val state =
            CuratedActorMoviesPager(repository = repository, actorName = "Actor A", pageSize = 2)
                .loadFirstPage()

        assertEquals(listOf(MovieRequest(limit = 2, offset = 0, actor = "Actor A")), repository.movieRequests)
        assertEquals(listOf("movie-1", "movie-2"), state.movies.map { it.id })
        assertEquals(5, state.total)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadNextPageRequestsOffsetFromLoadedMoviesAndPreservesActorFilter() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        MoviesPage(
                            items = listOf(movieListItem("movie-3"), movieListItem("movie-4")),
                            total = 5,
                            limit = 2,
                            offset = 2,
                        )
                    )
            )
        val current =
            CuratedActorMoviesState(
                isLoading = false,
                movies = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                total = 5,
            )

        val state =
            CuratedActorMoviesPager(repository = repository, actorName = "Actor A", pageSize = 2)
                .loadNextPage(current)

        assertEquals(listOf(MovieRequest(limit = 2, offset = 2, actor = "Actor A")), repository.movieRequests)
        assertEquals(
            listOf("movie-1", "movie-2", "movie-3", "movie-4"),
            state.movies.map { it.id },
        )
        assertEquals(5, state.total)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadNextPageDoesNotRequestWhenAllMoviesAreLoaded() = runBlocking {
        val repository = FakeCuratedRepository(pages = emptyList())
        val current =
            CuratedActorMoviesState(
                isLoading = false,
                movies = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                total = 2,
                endReached = true,
            )

        val state =
            CuratedActorMoviesPager(repository = repository, actorName = "Actor A", pageSize = 2)
                .loadNextPage(current)

        assertEquals(emptyList<MovieRequest>(), repository.movieRequests)
        assertEquals(current, state)
        assertEquals(false, state.canLoadMore)
    }

    private class FakeCuratedRepository(private val pages: List<MoviesPage>) : CuratedRepository {
        val movieRequests = mutableListOf<MovieRequest>()

        override suspend fun getHomepageRecommendations(): HomepageDailyRecommendations =
            error("Not used")

        override suspend fun getMovies(
            limit: Int,
            offset: Int,
            query: String?,
            actor: String?,
            studio: String?,
            mode: String?,
        ): MoviesPage {
            movieRequests += MovieRequest(limit = limit, offset = offset, actor = actor)
            return pages[movieRequests.lastIndex]
        }

        override suspend fun getActors(
            limit: Int,
            offset: Int,
            query: String?,
            actorTag: String?,
            sort: String?,
        ): ActorsPage = error("Not used")

        override suspend fun getActorProfile(name: String): ActorProfile = error("Not used")

        override suspend fun getMovie(movieId: String): MovieDetail = error("Not used")

        override suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor =
            error("Not used")

        override suspend fun getPlaybackProgress(): List<PlaybackProgress> = error("Not used")

        override suspend fun updatePlaybackProgress(
            movieId: String,
            positionSec: Double,
            durationSec: Double?,
        ) = error("Not used")
    }

    private data class MovieRequest(val limit: Int, val offset: Int, val actor: String? = null)

    private fun movieListItem(id: String): MovieListItem =
        MovieListItem(
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
            addedAt = "",
            location = "",
            resolution = "1080p",
            year = 2026,
            releaseDate = null,
            coverUrl = null,
            thumbUrl = null,
            trashedAt = null,
        )
}
