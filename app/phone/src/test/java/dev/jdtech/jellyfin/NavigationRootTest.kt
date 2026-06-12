package dev.jdtech.jellyfin

import dev.jdtech.jellyfin.core.presentation.dummy.dummyEpisode
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovie
import dev.jdtech.jellyfin.core.presentation.dummy.dummySeason
import dev.jdtech.jellyfin.core.presentation.dummy.dummyShow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRootTest {
    @Test
    fun curatedStartDestinationSkipsUserSelectionWhenCurrentServerExists() {
        assertEquals(
            HomeRoute,
            curatedStartDestination(
                hasServers = true,
                hasCurrentServer = true,
                hasCurrentUser = false,
            ),
        )
    }

    @Test
    fun curatedStartDestinationUsesAuthLockWhenCurrentServerIsLocked() {
        assertEquals(
            AuthLockRoute,
            curatedStartDestination(
                hasServers = true,
                hasCurrentServer = true,
                hasCurrentUser = false,
                isCuratedAuthLocked = true,
            ),
        )
    }

    @Test
    fun curatedNavigationItemsHideDownloadsForOnlineAndOfflineModes() {
        val onlineRoutes = curatedNavigationItems(isOfflineMode = false).map { it.route::class }
        val offlineRoutes = curatedNavigationItems(isOfflineMode = true).map { it.route::class }

        assertEquals(listOf(HomeRoute::class, MediaRoute::class), onlineRoutes)
        assertEquals(listOf(HomeRoute::class, MediaRoute::class), offlineRoutes)
        assertFalse(onlineRoutes.contains(DownloadsRoute::class))
        assertFalse(offlineRoutes.contains(DownloadsRoute::class))
    }

    @Test
    fun curatedRouteForItemReturnsNullForTvItems() {
        assertNull(curatedRouteForItem(dummyShow))
        assertNull(curatedRouteForItem(dummySeason))
        assertNull(curatedRouteForItem(dummyEpisode))
    }

    @Test
    fun curatedRouteForItemKeepsMovieRoute() {
        assertEquals(MovieRoute(dummyMovie.id.toString()), curatedRouteForItem(dummyMovie))
    }

    @Test
    fun curatedMovieIdForRouteKeepsNonUuidIds() {
        assertEquals("movie/ABC-001", curatedMovieIdForRoute(MovieRoute("movie/ABC-001")))
    }

    @Test
    fun isCuratedVisibleItemHidesTvItems() {
        assertTrue(isCuratedVisibleItem(dummyMovie))
        assertFalse(isCuratedVisibleItem(dummyShow))
        assertFalse(isCuratedVisibleItem(dummySeason))
        assertFalse(isCuratedVisibleItem(dummyEpisode))
    }
}
