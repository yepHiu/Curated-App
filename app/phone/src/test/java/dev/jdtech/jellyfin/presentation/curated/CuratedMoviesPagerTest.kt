package dev.jdtech.jellyfin.presentation.curated

import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.curated.api.MoviesPage
import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.PlaybackProgress
import dev.jdtech.jellyfin.curated.repository.CuratedRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedMoviesPagerTest {
    @Test
    fun loadFirstPageRequestsFirstOffsetAndStoresTotal() = runBlocking {
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

        val state = CuratedMoviesPager(repository, pageSize = 2).loadFirstPage()

        assertEquals(listOf(MovieRequest(limit = 2, offset = 0)), repository.movieRequests)
        assertEquals(listOf("movie-1", "movie-2"), state.movies.map { it.id })
        assertEquals(5, state.total)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadFirstPagePassesSearchQueryAndStoresVisibleQuery() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        MoviesPage(
                            items = listOf(movieListItem("movie-1")),
                            total = 1,
                            limit = 2,
                            offset = 0,
                        )
                    )
            )

        val state =
            CuratedMoviesPager(repository, pageSize = 2, query = "ABC")
                .loadFirstPage(searchQuery = " ABC ")

        assertEquals(listOf(MovieRequest(limit = 2, offset = 0, query = "ABC")), repository.movieRequests)
        assertEquals(" ABC ", state.searchQuery)
    }

    @Test
    fun loadNextPageRequestsOffsetFromLoadedMoviesAndAppendsItems() = runBlocking {
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
            CuratedMoviesState(
                isLoading = false,
                movies = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                total = 5,
            )

        val state = CuratedMoviesPager(repository, pageSize = 2).loadNextPage(current)

        assertEquals(listOf(MovieRequest(limit = 2, offset = 2)), repository.movieRequests)
        assertEquals(
            listOf("movie-1", "movie-2", "movie-3", "movie-4"),
            state.movies.map { it.id },
        )
        assertEquals(5, state.total)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadNextPagePreservesSearchQuery() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        MoviesPage(
                            items = listOf(movieListItem("movie-3"), movieListItem("movie-4")),
                            total = 4,
                            limit = 2,
                            offset = 2,
                        )
                    )
            )
        val current =
            CuratedMoviesState(
                isLoading = false,
                movies = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                total = 4,
                searchQuery = "ABC",
            )

        val state = CuratedMoviesPager(repository, pageSize = 2, query = "ABC").loadNextPage(current)

        assertEquals(listOf(MovieRequest(limit = 2, offset = 2, query = "ABC")), repository.movieRequests)
        assertEquals(listOf("movie-1", "movie-2", "movie-3", "movie-4"), state.movies.map { it.id })
        assertEquals("ABC", state.searchQuery)
        assertEquals(false, state.canLoadMore)
    }

    @Test
    fun normalizedSearchQueryTrimsBlankQueries() {
        assertEquals(null, curatedMoviesNormalizedSearchQuery(""))
        assertEquals(null, curatedMoviesNormalizedSearchQuery("   "))
        assertEquals("ABC", curatedMoviesNormalizedSearchQuery(" ABC "))
    }

    @Test
    fun loadNextPageDoesNotRequestWhenAllMoviesAreLoaded() = runBlocking {
        val repository = FakeCuratedRepository(pages = emptyList())
        val current =
            CuratedMoviesState(
                isLoading = false,
                movies = listOf(movieListItem("movie-1"), movieListItem("movie-2")),
                total = 2,
            )

        val state = CuratedMoviesPager(repository, pageSize = 2).loadNextPage(current)

        assertEquals(emptyList<MovieRequest>(), repository.movieRequests)
        assertEquals(current, state)
        assertEquals(false, state.canLoadMore)
    }

    private class FakeCuratedRepository(private val pages: List<MoviesPage>) : CuratedRepository {
        val movieRequests = mutableListOf<MovieRequest>()

        override suspend fun getMovies(
            limit: Int,
            offset: Int,
            query: String?,
            actor: String?,
            studio: String?,
            mode: String?,
        ): MoviesPage {
            movieRequests += MovieRequest(limit = limit, offset = offset, query = query)
            return pages[movieRequests.lastIndex]
        }

        override suspend fun getMovie(movieId: String): MovieDetail = error("Not used")

        override suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor =
            error("Not used")

        override suspend fun getPlaybackProgress(): List<PlaybackProgress> = error("Not used")
    }

    private data class MovieRequest(val limit: Int, val offset: Int, val query: String? = null)

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
