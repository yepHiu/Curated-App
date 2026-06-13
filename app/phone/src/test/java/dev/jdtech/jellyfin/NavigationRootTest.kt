package dev.jdtech.jellyfin

import dev.jdtech.jellyfin.core.presentation.dummy.dummyEpisode
import dev.jdtech.jellyfin.core.presentation.dummy.dummyMovie
import dev.jdtech.jellyfin.core.presentation.dummy.dummySeason
import dev.jdtech.jellyfin.core.presentation.dummy.dummyShow
import dev.jdtech.jellyfin.core.presentation.theme.ColorDark
import androidx.compose.ui.unit.dp
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

        assertEquals(
            listOf(
                HomeRoute::class,
                MediaRoute::class,
                ActorsRoute::class,
                HistoryRoute::class,
                SettingsRoute::class,
            ),
            onlineRoutes,
        )
        assertEquals(
            listOf(
                HomeRoute::class,
                MediaRoute::class,
                ActorsRoute::class,
                HistoryRoute::class,
                SettingsRoute::class,
            ),
            offlineRoutes,
        )
        assertFalse(onlineRoutes.contains(DownloadsRoute::class))
        assertFalse(offlineRoutes.contains(DownloadsRoute::class))
    }

    @Test
    fun curatedNavigationItemsIncludeSettingsForSidebar() {
        val routes = curatedNavigationItems(isOfflineMode = false).map { it.route::class }

        assertEquals(
            listOf(
                HomeRoute::class,
                MediaRoute::class,
                ActorsRoute::class,
                HistoryRoute::class,
                SettingsRoute::class,
            ),
            routes,
        )
    }

    @Test
    fun curatedNavigationLayoutUsesModalDrawerOnCompactWidth() {
        assertEquals(
            CuratedNavigationLayout.ModalDrawer,
            curatedNavigationLayoutType(isExpandedWidth = false),
        )
    }

    @Test
    fun curatedNavigationLayoutUsesPermanentDrawerOnExpandedWidth() {
        assertEquals(
            CuratedNavigationLayout.PermanentDrawer,
            curatedNavigationLayoutType(isExpandedWidth = true),
        )
    }

    @Test
    fun curatedNavigationSelectionMapsDetailRoutesToParentItems() {
        assertEquals(
            MediaRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(MovieRoute("movie-1")::class.qualifiedName),
        )
        assertEquals(
            ActorsRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(ActorRoute("Actor A")::class.qualifiedName),
        )
        assertEquals(
            SettingsRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(AboutRoute::class.qualifiedName),
        )
    }

    @Test
    fun curatedNavigationDrawerOnlyEnablesForAppRoutes() {
        val navigationItems = curatedNavigationItems(isOfflineMode = false)

        assertTrue(
            curatedNavigationDrawerEnabled(
                selectedRoute = HomeRoute::class.qualifiedName,
                navigationItems = navigationItems,
            )
        )
        assertTrue(
            curatedNavigationDrawerEnabled(
                selectedRoute = MediaRoute::class.qualifiedName,
                navigationItems = navigationItems,
            )
        )
        assertFalse(
            curatedNavigationDrawerEnabled(
                selectedRoute = WelcomeRoute::class.qualifiedName,
                navigationItems = navigationItems,
            )
        )
    }

    @Test
    fun curatedNavigationDrawerUsesCompactWidths() {
        assertEquals(256.dp, curatedNavigationDrawerWidth(CuratedNavigationLayout.ModalDrawer))
        assertEquals(224.dp, curatedNavigationDrawerWidth(CuratedNavigationLayout.PermanentDrawer))
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

    @Test
    fun curatedNavigationSelectedLabelUsesHighContrastContentColor() {
        val colors =
            curatedNavigationItemColorSpec(
                selectedContentColor = ColorDark.onSurfaceDark,
                unselectedContentColor = ColorDark.onSurfaceVariantDark,
                selectedIndicatorColor = ColorDark.primaryContainerDark,
            )

        assertEquals(ColorDark.onSurfaceDark, colors.selectedTextColor)
        assertEquals(ColorDark.onSurfaceDark, colors.selectedIconColor)
        assertEquals(ColorDark.onSurfaceVariantDark, colors.unselectedTextColor)
        assertEquals(ColorDark.onSurfaceVariantDark, colors.unselectedIconColor)
        assertEquals(ColorDark.primaryContainerDark, colors.selectedIndicatorColor)
    }
}
