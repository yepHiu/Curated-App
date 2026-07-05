package dev.curated.app.presentation.curated

import dev.curated.app.curated.api.MovieListItem
import dev.curated.app.curated.repository.CuratedRepository

internal class CuratedActorMoviesPager(
    private val repository: CuratedRepository,
    private val actorName: String,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    suspend fun loadFirstPage(): CuratedActorMoviesState {
        val page = repository.getMovies(limit = pageSize, offset = 0, actor = actorName)
        return CuratedActorMoviesState(
            isLoading = false,
            movies = page.items,
            total = page.total,
            endReached = page.items.isEmpty() || page.items.size >= page.total,
        )
    }

    suspend fun loadNextPage(current: CuratedActorMoviesState): CuratedActorMoviesState {
        if (!current.canLoadMore) return current

        val page =
            repository.getMovies(
                limit = pageSize,
                offset = current.movies.size,
                actor = actorName,
            )
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

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}

data class CuratedActorMoviesState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val movies: List<MovieListItem> = emptyList(),
    val total: Int = 0,
    val errorMessage: String? = null,
    val appendErrorMessage: String? = null,
    val endReached: Boolean = false,
) {
    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && !endReached && movies.size < total
}
