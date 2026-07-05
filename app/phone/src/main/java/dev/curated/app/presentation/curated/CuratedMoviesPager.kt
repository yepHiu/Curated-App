package dev.curated.app.presentation.curated

import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.curated.repository.CuratedRepository

internal class CuratedMoviesPager(
    private val repository: CuratedRepository,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val query: String? = null,
) {
    suspend fun loadFirstPage(searchQuery: String = query.orEmpty()): CuratedMoviesState {
        val page = repository.getMovies(limit = pageSize, offset = 0, query = query)
        return CuratedMoviesState(
            isLoading = false,
            movies = page.items,
            total = page.total,
            searchQuery = searchQuery,
            playbackProgressByMovieId = loadPlaybackProgressByMovieId(),
            endReached = page.items.isEmpty() || page.items.size >= page.total,
        )
    }

    suspend fun loadNextPage(current: CuratedMoviesState): CuratedMoviesState {
        if (!current.canLoadMore) return current

        val page =
            repository.getMovies(limit = pageSize, offset = current.movies.size, query = query)
        val movies = current.movies + page.items
        return current.copy(
            isLoading = false,
            isLoadingMore = false,
            movies = movies,
            total = page.total,
            appendErrorMessage = null,
            endReached = page.items.isEmpty() || movies.size >= page.total,
        )
    }

    private suspend fun loadPlaybackProgressByMovieId(): Map<String, PlaybackProgress> =
        runCatching { repository.getPlaybackProgress().associateBy { it.movieId } }.getOrDefault(emptyMap())

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}

internal fun curatedMoviesNormalizedSearchQuery(query: String): String? =
    query.trim().takeIf { it.isNotEmpty() }
