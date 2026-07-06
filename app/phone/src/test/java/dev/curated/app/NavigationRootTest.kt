package dev.curated.app

import dev.curated.app.core.presentation.dummy.dummyEpisode
import dev.curated.app.core.presentation.dummy.dummyMovie
import dev.curated.app.core.presentation.dummy.dummySeason
import dev.curated.app.core.presentation.dummy.dummyShow
import dev.curated.app.core.presentation.theme.ColorDark
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
                MyRoute::class,
                SettingsRoute::class,
            ),
            onlineRoutes,
        )
        assertEquals(
            listOf(
                HomeRoute::class,
                MediaRoute::class,
                MyRoute::class,
                SettingsRoute::class,
            ),
            offlineRoutes,
        )
        assertFalse(onlineRoutes.contains(DownloadsRoute::class))
        assertFalse(offlineRoutes.contains(DownloadsRoute::class))
    }

    @Test
    fun curatedBottomNavigationItemsOnlyExposePrimaryDestinations() {
        val routes = curatedBottomNavigationItems().map { it.route::class }

        assertEquals(
            listOf(HomeRoute::class, MediaRoute::class, MyRoute::class, SettingsRoute::class),
            routes,
        )
    }

    @Test
    fun curatedDrawerNavigationItemsAreEmptyWhenSecondaryDestinationsMoveUnderMy() {
        val routes = curatedDrawerNavigationItems(isOfflineMode = false).map { it.route::class }

        assertTrue(routes.isEmpty())
    }

    @Test
    fun curatedFloatingNavigationBarOnlyShowsOnTopLevelAppRoutes() {
        assertTrue(curatedFloatingNavigationBarVisible(HomeRoute::class.qualifiedName))
        assertTrue(curatedFloatingNavigationBarVisible(MediaRoute::class.qualifiedName))
        assertTrue(curatedFloatingNavigationBarVisible(MyRoute::class.qualifiedName))
        assertTrue(curatedFloatingNavigationBarVisible(ActorsRoute::class.qualifiedName))
        assertTrue(curatedFloatingNavigationBarVisible(HistoryRoute::class.qualifiedName))
        assertTrue(curatedFloatingNavigationBarVisible(SettingsRoute::class.qualifiedName))
        assertTrue(
            curatedFloatingNavigationBarVisible(
                "${SettingsRoute::class.qualifiedName}/{indexes}"
            )
        )
        assertTrue(
            curatedFloatingNavigationBarVisible(
                "${SettingsRoute::class.qualifiedName}?indexes={indexes}"
            )
        )

        assertFalse(curatedFloatingNavigationBarVisible(MovieRoute::class.qualifiedName))
        assertFalse(curatedFloatingNavigationBarVisible(WelcomeRoute::class.qualifiedName))
        assertFalse(curatedFloatingNavigationBarVisible(AboutRoute::class.qualifiedName))
    }

    @Test
    fun curatedRouteMatchingAcceptsTypedNavigationArgumentPatterns() {
        assertTrue(
            curatedRouteMatches(
                currentRoute = "${SettingsRoute::class.qualifiedName}/{indexes}",
                routeQualifiedName = SettingsRoute::class.qualifiedName,
            )
        )
        assertTrue(
            curatedRouteMatches(
                currentRoute = "${SettingsRoute::class.qualifiedName}?indexes={indexes}",
                routeQualifiedName = SettingsRoute::class.qualifiedName,
            )
        )
        assertFalse(
            curatedRouteMatches(
                currentRoute = "${SettingsRoute::class.qualifiedName}Extra",
                routeQualifiedName = SettingsRoute::class.qualifiedName,
            )
        )
    }

    @Test
    fun curatedFloatingNavigationContentPaddingClearsFloatingBar() {
        assertEquals(92.dp, curatedFloatingNavigationContentBottomPadding(0.dp))
        assertEquals(116.dp, curatedFloatingNavigationContentBottomPadding(24.dp))
    }

    @Test
    fun curatedFloatingNavigationUsesCompactCuratedSurfaceChrome() {
        assertEquals(58.dp, curatedFloatingNavigationBarHeight())
        assertEquals(46.dp, curatedFloatingNavigationItemHeight())
        assertEquals(10.dp, curatedFloatingNavigationBarBottomMargin())
        assertEquals(520.dp, curatedFloatingNavigationBarMaxWidth())
        assertEquals(0.94f, curatedFloatingNavigationContainerAlpha(isDarkTheme = true))
        assertEquals(0.96f, curatedFloatingNavigationContainerAlpha(isDarkTheme = false))
        assertTrue(
            curatedFloatingNavigationContainerAlpha(isDarkTheme = true) >
                curatedFloatingNavigationSelectedContainerAlpha(isDarkTheme = true)
        )
        assertTrue(
            curatedFloatingNavigationContainerAlpha(isDarkTheme = false) >
                curatedFloatingNavigationSelectedContainerAlpha(isDarkTheme = false)
        )
    }

    @Test
    fun curatedFloatingNavigationDarkChromeUsesCuratedSurfaceInsteadOfInverseSurface() {
        val color =
            curatedFloatingNavigationContainerBaseColor(
                surfaceContainerHigh = ColorDark.surfaceContainerHighDark,
            )

        assertEquals(ColorDark.surfaceContainerHighDark, color)
        assertNotEquals(ColorDark.inverseSurfaceDark, color)
    }

    @Test
    fun curatedFloatingNavigationBottomScrimFadesBehindFloatingBar() {
        assertEquals(92.dp, curatedFloatingNavigationBottomScrimHeight(0.dp))
        assertEquals(116.dp, curatedFloatingNavigationBottomScrimHeight(24.dp))
        assertEquals(0f, curatedFloatingNavigationBottomScrimTopAlpha(), 0.001f)
        assertEquals(0.82f, curatedFloatingNavigationBottomScrimBottomAlpha(), 0.001f)
    }

    @Test
    fun curatedFloatingNavigationSelectedIndicatorSlidesBetweenEqualItems() {
        assertEquals(
            0.dp,
            curatedFloatingNavigationSelectionIndicatorOffset(
                itemWidth = 100.dp,
                itemSpacing = 4.dp,
                selectedIndex = 0,
            ),
        )
        assertEquals(
            104.dp,
            curatedFloatingNavigationSelectionIndicatorOffset(
                itemWidth = 100.dp,
                itemSpacing = 4.dp,
                selectedIndex = 1,
            ),
        )
        assertEquals(0.82f, curatedFloatingNavigationSelectionAnimationDampingRatio(), 0.001f)
        assertEquals(420f, curatedFloatingNavigationSelectionAnimationStiffness(), 0.001f)
    }

    @Test
    fun curatedFloatingNavigationUsesSingleSelectionStateLayer() {
        assertFalse(curatedFloatingNavigationItemPressStateLayerEnabled())
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
            MediaRoute::class.qualifiedName,
            curatedNavigationSelectedRoute("${MovieRoute::class.qualifiedName}/{movieId}"),
        )
        assertEquals(
            MyRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(ActorRoute("Actor A")::class.qualifiedName),
        )
        assertEquals(
            MyRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(ActorsRoute::class.qualifiedName),
        )
        assertEquals(
            MyRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(HistoryRoute::class.qualifiedName),
        )
        assertEquals(
            SettingsRoute::class.qualifiedName,
            curatedNavigationSelectedRoute(AboutRoute::class.qualifiedName),
        )
        assertEquals(
            SettingsRoute::class.qualifiedName,
            curatedNavigationSelectedRoute("${SettingsRoute::class.qualifiedName}/{indexes}"),
        )
    }

    @Test
    fun curatedNavigationDrawerDisablesWhenSecondaryDestinationsMoveUnderMy() {
        val navigationItems = curatedDrawerNavigationItems(isOfflineMode = false)

        assertFalse(
            curatedNavigationDrawerEnabled(
                selectedRoute = HomeRoute::class.qualifiedName,
                navigationItems = navigationItems,
            )
        )
        assertFalse(
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
