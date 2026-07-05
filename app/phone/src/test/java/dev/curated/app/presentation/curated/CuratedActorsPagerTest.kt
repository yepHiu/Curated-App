package dev.curated.app.presentation.curated

import dev.curated.app.curated.api.ActorListItem
import dev.curated.app.curated.api.ActorProfile
import dev.curated.app.curated.api.ActorsPage
import dev.curated.app.curated.api.HomepageDailyRecommendations
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.api.MoviesPage
import dev.curated.app.curated.api.PlaybackDescriptor
import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.curated.repository.CuratedRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedActorsPagerTest {
    @Test
    fun loadFirstPageRequestsFirstOffsetAndStoresTotalSearchAndSort() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        ActorsPage(
                            actors = listOf(actorListItem("Actor A"), actorListItem("Actor B")),
                            total = 5,
                        )
                    )
            )

        val state =
            CuratedActorsPager(
                    repository = repository,
                    pageSize = 2,
                    query = "Actor",
                    sort = "movieCount",
                )
                .loadFirstPage(searchQuery = " Actor ")

        assertEquals(
            listOf(
                ActorRequest(
                    limit = 2,
                    offset = 0,
                    query = "Actor",
                    sort = "movieCount",
                )
            ),
            repository.actorRequests,
        )
        assertEquals(listOf("Actor A", "Actor B"), state.actors.map { it.name })
        assertEquals(5, state.total)
        assertEquals(" Actor ", state.searchQuery)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadNextPageRequestsOffsetFromLoadedActorsAndAppendsItems() = runBlocking {
        val repository =
            FakeCuratedRepository(
                pages =
                    listOf(
                        ActorsPage(
                            actors = listOf(actorListItem("Actor C"), actorListItem("Actor D")),
                            total = 5,
                        )
                    )
            )
        val current =
            CuratedActorsState(
                isLoading = false,
                actors = listOf(actorListItem("Actor A"), actorListItem("Actor B")),
                total = 5,
                searchQuery = "Actor",
            )

        val state =
            CuratedActorsPager(
                    repository = repository,
                    pageSize = 2,
                    query = "Actor",
                    sort = "movieCount",
                )
                .loadNextPage(current)

        assertEquals(
            listOf(
                ActorRequest(
                    limit = 2,
                    offset = 2,
                    query = "Actor",
                    sort = "movieCount",
                )
            ),
            repository.actorRequests,
        )
        assertEquals(
            listOf("Actor A", "Actor B", "Actor C", "Actor D"),
            state.actors.map { it.name },
        )
        assertEquals(5, state.total)
        assertEquals("Actor", state.searchQuery)
        assertEquals(true, state.canLoadMore)
    }

    @Test
    fun loadNextPageDoesNotRequestWhenAllActorsAreLoaded() = runBlocking {
        val repository = FakeCuratedRepository(pages = emptyList())
        val current =
            CuratedActorsState(
                isLoading = false,
                actors = listOf(actorListItem("Actor A"), actorListItem("Actor B")),
                total = 2,
                endReached = true,
            )

        val state = CuratedActorsPager(repository = repository, pageSize = 2).loadNextPage(current)

        assertEquals(emptyList<ActorRequest>(), repository.actorRequests)
        assertEquals(current, state)
        assertEquals(false, state.canLoadMore)
    }

    @Test
    fun normalizedSearchQueryTrimsBlankQueries() {
        assertEquals(null, curatedActorsNormalizedSearchQuery(""))
        assertEquals(null, curatedActorsNormalizedSearchQuery("   "))
        assertEquals("Actor", curatedActorsNormalizedSearchQuery(" Actor "))
    }

    private class FakeCuratedRepository(private val pages: List<ActorsPage>) : CuratedRepository {
        val actorRequests = mutableListOf<ActorRequest>()

        override suspend fun getHomepageRecommendations(): HomepageDailyRecommendations =
            error("Not used")

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
        ): ActorsPage {
            actorRequests +=
                ActorRequest(
                    limit = limit,
                    offset = offset,
                    query = query,
                    actorTag = actorTag,
                    sort = sort,
                )
            return pages[actorRequests.lastIndex]
        }

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

    private data class ActorRequest(
        val limit: Int,
        val offset: Int,
        val query: String? = null,
        val actorTag: String? = null,
        val sort: String? = null,
    )

    private fun actorListItem(name: String): ActorListItem =
        ActorListItem(
            name = name,
            avatarUrl = null,
            avatarRemoteUrl = null,
            avatarLocalUrl = null,
            hasLocalAvatar = false,
            movieCount = 2,
            userTags = emptyList(),
        )
}
