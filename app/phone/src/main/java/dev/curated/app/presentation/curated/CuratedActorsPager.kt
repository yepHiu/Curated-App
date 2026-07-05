package dev.curated.app.presentation.curated

import dev.curated.app.curated.api.ActorListItem
import dev.curated.app.curated.repository.CuratedRepository

internal class CuratedActorsPager(
    private val repository: CuratedRepository,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val query: String? = null,
    private val actorTag: String? = null,
    private val sort: String? = null,
) {
    suspend fun loadFirstPage(searchQuery: String = query.orEmpty()): CuratedActorsState {
        val page =
            repository.getActors(
                limit = pageSize,
                offset = 0,
                query = query,
                actorTag = actorTag,
                sort = sort,
            )
        return CuratedActorsState(
            isLoading = false,
            actors = page.actors,
            total = page.total,
            searchQuery = searchQuery,
            endReached = page.actors.isEmpty() || page.actors.size >= page.total,
        )
    }

    suspend fun loadNextPage(current: CuratedActorsState): CuratedActorsState {
        if (!current.canLoadMore) return current

        val page =
            repository.getActors(
                limit = pageSize,
                offset = current.actors.size,
                query = query,
                actorTag = actorTag,
                sort = sort,
            )
        val actors = current.actors + page.actors
        return current.copy(
            isLoading = false,
            isLoadingMore = false,
            actors = actors,
            total = page.total,
            appendErrorMessage = null,
            endReached = page.actors.isEmpty() || actors.size >= page.total,
        )
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}

data class CuratedActorsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val actors: List<ActorListItem> = emptyList(),
    val total: Int = 0,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val appendErrorMessage: String? = null,
    val endReached: Boolean = false,
) {
    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && !endReached && actors.size < total
}

internal fun curatedActorsNormalizedSearchQuery(query: String): String? =
    query.trim().takeIf { it.isNotEmpty() }
