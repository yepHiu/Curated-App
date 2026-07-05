package dev.curated.app

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import dev.curated.app.core.R as CoreR
import dev.curated.app.models.CollectionType
import dev.curated.app.models.FindroidBoxSet
import dev.curated.app.models.FindroidCollection
import dev.curated.app.models.FindroidEpisode
import dev.curated.app.models.FindroidFolder
import dev.curated.app.models.FindroidItem
import dev.curated.app.models.FindroidMovie
import dev.curated.app.models.FindroidSeason
import dev.curated.app.models.FindroidShow
import dev.curated.app.presentation.film.CollectionScreen
import dev.curated.app.presentation.film.DownloadsScreen
import dev.curated.app.presentation.film.EpisodeScreen
import dev.curated.app.presentation.film.FavoritesScreen
import dev.curated.app.presentation.film.HomeScreen
import dev.curated.app.presentation.film.LibraryScreen
import dev.curated.app.presentation.film.MediaScreen
import dev.curated.app.presentation.film.MovieScreen
import dev.curated.app.presentation.film.PersonScreen
import dev.curated.app.presentation.film.SeasonScreen
import dev.curated.app.presentation.film.ShowScreen
import dev.curated.app.presentation.curated.CuratedActorDetailScreen
import dev.curated.app.presentation.curated.CuratedActorsScreen
import dev.curated.app.presentation.curated.CuratedHistoryScreen
import dev.curated.app.presentation.curated.CuratedHomeScreen
import dev.curated.app.presentation.curated.CuratedMovieDetailScreen
import dev.curated.app.presentation.curated.CuratedMoviesScreen
import dev.curated.app.presentation.settings.AboutScreen
import dev.curated.app.presentation.settings.SettingsScreen
import dev.curated.app.presentation.setup.addresses.ServerAddressesScreen
import dev.curated.app.presentation.setup.addserver.AddServerScreen
import dev.curated.app.presentation.setup.authlock.AuthLockScreen
import dev.curated.app.presentation.setup.login.LoginScreen
import dev.curated.app.presentation.setup.servers.ServersScreen
import dev.curated.app.presentation.setup.users.UsersScreen
import dev.curated.app.presentation.setup.welcome.WelcomeScreen
import dev.curated.app.presentation.utils.LocalOfflineMode
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable data object WelcomeRoute

@Serializable data object ServersRoute

@Serializable data object AddServerRoute

@Serializable data object AuthLockRoute

@Serializable data class ServerAddressesRoute(val serverId: String)

@Serializable data object UsersRoute

@Serializable data class LoginRoute(val username: String? = null)

@Serializable data object HomeRoute

@Serializable data object MediaRoute

@Serializable data object ActorsRoute

@Serializable data class ActorRoute(val name: String)

@Serializable data object HistoryRoute

@Serializable data object DownloadsRoute

@Serializable
data class LibraryRoute(
    val libraryId: String,
    val libraryName: String,
    val libraryType: CollectionType,
)

@Serializable data class CollectionRoute(val collectionId: String, val collectionName: String)

@Serializable data object FavoritesRoute

@Serializable data class MovieRoute(val movieId: String)

@Serializable data class ShowRoute(val showId: String)

@Serializable data class EpisodeRoute(val episodeId: String)

@Serializable data class SeasonRoute(val seasonId: String)

@Serializable data class PersonRoute(val personId: String)

@Serializable data class SettingsRoute(val indexes: IntArray)

@Serializable data object AboutRoute

data class TabBarItem(
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int,
    val route: Any,
    val enabled: Boolean = true,
)

val homeTab =
    TabBarItem(title = CoreR.string.title_home, icon = CoreR.drawable.ic_home, route = HomeRoute)
val mediaTab =
    TabBarItem(
        title = CoreR.string.title_media,
        icon = CoreR.drawable.ic_library,
        route = MediaRoute,
    )
val actorsTab =
    TabBarItem(
        title = CoreR.string.title_actors,
        icon = CoreR.drawable.ic_user,
        route = ActorsRoute,
    )
val historyTab =
    TabBarItem(
        title = CoreR.string.title_history,
        icon = CoreR.drawable.ic_history,
        route = HistoryRoute,
    )
val settingsTab =
    TabBarItem(
        title = CoreR.string.title_settings,
        icon = CoreR.drawable.ic_settings,
        route = SettingsRoute(indexes = intArrayOf(CoreR.string.title_settings)),
    )
val downloadsTab =
    TabBarItem(
        title = CoreR.string.title_download,
        icon = CoreR.drawable.ic_download,
        route = DownloadsRoute,
    )

private val CuratedFloatingNavigationBarHeight = 58.dp
private val CuratedFloatingNavigationBarBottomMargin = 16.dp
private val CuratedFloatingNavigationContentExtraScrollClearance = 24.dp
private val CuratedFloatingNavigationBarMaxWidth = 520.dp
private val CuratedFloatingNavigationItemHeight = 46.dp

@Composable
fun NavigationRoot(
    navController: NavHostController,
    hasServers: Boolean,
    hasCurrentServer: Boolean,
    hasCurrentUser: Boolean,
    isCuratedAuthLocked: Boolean = false,
) {
    val isOfflineMode = LocalOfflineMode.current
    val context = LocalContext.current

    val startDestination =
        curatedStartDestination(
            hasServers = hasServers,
            hasCurrentServer = hasCurrentServer,
            hasCurrentUser = hasCurrentUser,
            isCuratedAuthLocked = isCuratedAuthLocked,
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedNavigationRoute = curatedNavigationSelectedRoute(currentRoute)
    val bottomNavigationVisible = curatedFloatingNavigationBarVisible(currentRoute)
    val safeDrawingBottom =
        with(LocalDensity.current) { WindowInsets.safeDrawing.getBottom(this).toDp() }
    val floatingNavigationContentBottomPadding =
        if (bottomNavigationVisible) {
            curatedFloatingNavigationContentBottomPadding(safeDrawingBottom)
        } else {
            0.dp
        }

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val navigationLayout =
        curatedNavigationLayoutType(
            isExpandedWidth =
                windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                )
        )
    val navigationItems = curatedNavigationItems(isOfflineMode)
    val bottomNavigationItems = curatedBottomNavigationItems()
    val drawerNavigationItems = curatedDrawerNavigationItems(isOfflineMode)
    val navigationDrawerEnabled =
        curatedNavigationDrawerEnabled(
            selectedRoute = selectedNavigationRoute,
            navigationItems = navigationItems,
        )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val onOpenNavigation: (() -> Unit)? =
        if (navigationLayout == CuratedNavigationLayout.ModalDrawer && navigationDrawerEnabled) {
            { coroutineScope.launch { drawerState.open() } }
        } else {
            null
        }
    val onNavigationItemClick: (TabBarItem) -> Unit = { item ->
        navController.safeNavigate(item.route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        if (navigationLayout == CuratedNavigationLayout.ModalDrawer) {
            coroutineScope.launch { drawerState.close() }
        }
    }

    val navigationContent: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) },
        ) {
            composable<WelcomeRoute> {
                WelcomeScreen(onContinueClick = { navController.safeNavigate(ServersRoute) })
            }
            composable<ServersRoute> {
                ServersScreen(
                    navigateHome = { navigateHomeAndClearBackStack(navController) },
                    navigateToAuthLock = { navigateAuthGate(navController) },
                    navigateToAddresses = { serverId ->
                        navController.safeNavigate(ServerAddressesRoute(serverId))
                    },
                    onAddClick = { navController.safeNavigate(AddServerRoute) },
                    onBackClick = { navController.safePopBackStack() },
                    showBack = navController.previousBackStackEntry != null,
                )
            }
            composable<AddServerRoute> {
                AddServerScreen(
                    onSuccess = { navigateHomeAndClearBackStack(navController) },
                    onAuthLockRequired = { navigateAuthGate(navController) },
                    onBackClick = { navController.safePopBackStack() },
                )
            }
            composable<AuthLockRoute> {
                AuthLockScreen(
                    onSuccess = { navigateHomeAndClearBackStack(navController) },
                    onChangeServerClick = {
                        navController.safeNavigate(ServersRoute) {
                            popUpTo(ServersRoute) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<ServerAddressesRoute> { backStackEntry ->
                val route: ServerAddressesRoute = backStackEntry.toRoute()
                ServerAddressesScreen(
                    serverId = route.serverId,
                    navigateBack = { navController.safePopBackStack() },
                )
            }
            composable<UsersRoute> {
                UsersScreen(
                    navigateToHome = { navigateHome(navController) },
                    onChangeServerClick = {
                        navController.safeNavigate(ServersRoute) {
                            popUpTo(ServersRoute) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onAddClick = { navController.safeNavigate(LoginRoute()) },
                    onBackClick = { navController.safePopBackStack() },
                    onPublicUserClick = { username ->
                        navController.safeNavigate(LoginRoute(username = username))
                    },
                    showBack = navController.previousBackStackEntry != null,
                )
            }
            composable<LoginRoute> { backStackEntry ->
                val route: LoginRoute = backStackEntry.toRoute()
                LoginScreen(
                    onSuccess = {
                        navController.safeNavigate(HomeRoute) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onChangeServerClick = {
                        navController.safeNavigate(ServersRoute) {
                            popUpTo(ServersRoute) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.safePopBackStack() },
                    prefilledUsername = route.username,
                )
            }
            composable<HomeRoute> {
                CuratedHomeScreen(
                    onOpenNavigation = onOpenNavigation,
                    onMovieClick = { movieId -> navController.safeNavigate(MovieRoute(movieId)) },
                    onPlayMovie = { movieId, title ->
                        context.startActivity(
                            Intent(context, CuratedPlayerActivity::class.java).apply {
                                putExtra(CuratedPlayerContract.EXTRA_MOVIE_ID, movieId)
                                putExtra(CuratedPlayerContract.EXTRA_TITLE, title)
                            }
                        )
                    },
                    onOpenMediaClick = { navController.safeNavigate(MediaRoute) },
                    bottomContentPadding = floatingNavigationContentBottomPadding,
                )
            }
            composable<MediaRoute> {
                CuratedMoviesScreen(
                    onOpenNavigation = onOpenNavigation,
                    onMovieClick = { movieId -> navController.safeNavigate(MovieRoute(movieId)) },
                    bottomContentPadding = floatingNavigationContentBottomPadding,
                )
            }
            composable<ActorsRoute> {
                CuratedActorsScreen(
                    onOpenNavigation = onOpenNavigation,
                    onActorClick = { actorName -> navController.safeNavigate(ActorRoute(actorName)) },
                    bottomContentPadding = floatingNavigationContentBottomPadding,
                )
            }
            composable<ActorRoute> { backStackEntry ->
                val route: ActorRoute = backStackEntry.toRoute()
                CuratedActorDetailScreen(
                    actorName = route.name,
                    navigateBack = { navController.safePopBackStack() },
                    onMovieClick = { movieId -> navController.safeNavigate(MovieRoute(movieId)) },
                )
            }
            composable<HistoryRoute> {
                CuratedHistoryScreen(
                    onOpenNavigation = onOpenNavigation,
                    onPlayMovie = { movieId, title ->
                        val extras = curatedHistoryPlayerExtras(movieId = movieId, title = title)
                        context.startActivity(
                            Intent(context, CuratedPlayerActivity::class.java).apply {
                                putExtra(
                                    CuratedPlayerContract.EXTRA_MOVIE_ID,
                                    extras[CuratedPlayerContract.EXTRA_MOVIE_ID],
                                )
                                putExtra(
                                    CuratedPlayerContract.EXTRA_TITLE,
                                    extras[CuratedPlayerContract.EXTRA_TITLE],
                                )
                            }
                        )
                    },
                    bottomContentPadding = floatingNavigationContentBottomPadding,
                )
            }
            composable<DownloadsRoute> {
                DownloadsScreen(
                    onItemClick = { item ->
                        navigateToItem(navController = navController, item = item)
                    }
                )
            }
            composable<LibraryRoute> { backStackEntry ->
                val route: LibraryRoute = backStackEntry.toRoute()
                LibraryScreen(
                    libraryId = UUID.fromString(route.libraryId),
                    libraryName = route.libraryName,
                    libraryType = route.libraryType,
                    onItemClick = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                    navigateBack = { navController.safePopBackStack() },
                )
            }
            composable<CollectionRoute> { backStackEntry ->
                val route: CollectionRoute = backStackEntry.toRoute()
                CollectionScreen(
                    collectionId = UUID.fromString(route.collectionId),
                    collectionName = route.collectionName,
                    onItemClick = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                    navigateBack = { navController.safePopBackStack() },
                )
            }
            composable<FavoritesRoute> {
                FavoritesScreen(
                    onItemClick = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                    navigateBack = { navController.safePopBackStack() },
                )
            }
            composable<MovieRoute> { backStackEntry ->
                val route: MovieRoute = backStackEntry.toRoute()
                CuratedMovieDetailScreen(
                    movieId = curatedMovieIdForRoute(route),
                    navigateBack = { navController.safePopBackStack() },
                    onPlayMovie = { movieId, title ->
                        context.startActivity(
                            Intent(context, CuratedPlayerActivity::class.java).apply {
                                putExtra(CuratedPlayerContract.EXTRA_MOVIE_ID, movieId)
                                putExtra(CuratedPlayerContract.EXTRA_TITLE, title)
                            }
                        )
                    },
                    onActorClick = { actorName -> navController.safeNavigate(ActorRoute(actorName)) },
                )
            }
            composable<ShowRoute> { backStackEntry ->
                val route: ShowRoute = backStackEntry.toRoute()
                ShowScreen(
                    showId = UUID.fromString(route.showId),
                    navigateBack = { navController.safePopBackStack() },
                    navigateHome = { navigateHome(navController) },
                    navigateToItem = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                    navigateToPerson = { personId ->
                        navController.safeNavigate(PersonRoute(personId.toString()))
                    },
                )
            }
            composable<SeasonRoute> { backStackEntry ->
                val route: SeasonRoute = backStackEntry.toRoute()
                SeasonScreen(
                    seasonId = UUID.fromString(route.seasonId),
                    navigateBack = { navController.safePopBackStack() },
                    navigateHome = { navigateHome(navController) },
                    navigateToItem = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                    navigateToSeries = { seriesId ->
                        navController.safeNavigate(ShowRoute(showId = seriesId.toString())) {
                            popUpTo(ShowRoute(showId = seriesId.toString()))
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<EpisodeRoute> { backStackEntry ->
                val route: EpisodeRoute = backStackEntry.toRoute()
                EpisodeScreen(
                    episodeId = UUID.fromString(route.episodeId),
                    navigateBack = { navController.safePopBackStack() },
                    navigateHome = { navigateHome(navController) },
                    navigateToPerson = { personId ->
                        navController.safeNavigate(PersonRoute(personId.toString()))
                    },
                    navigateToSeason = { seasonId ->
                        navController.safeNavigate(SeasonRoute(seasonId = seasonId.toString())) {
                            popUpTo(SeasonRoute(seasonId = seasonId.toString()))
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<PersonRoute> { backStackEntry ->
                val route: PersonRoute = backStackEntry.toRoute()
                PersonScreen(
                    personId = UUID.fromString(route.personId),
                    navigateBack = { navController.safePopBackStack() },
                    navigateHome = { navigateHome(navController) },
                    navigateToItem = { item ->
                        navigateToItem(navController = navController, item = item)
                    },
                )
            }
            composable<SettingsRoute> { backStackEntry ->
                val route: SettingsRoute = backStackEntry.toRoute()
                SettingsScreen(
                    indexes = route.indexes,
                    navigateToSettings = { indexes ->
                        navController.safeNavigate(SettingsRoute(indexes = indexes))
                    },
                    navigateToServers = { navController.safeNavigate(ServersRoute) },
                    navigateToUsers = {},
                    navigateToAbout = { navController.safeNavigate(AboutRoute) },
                    navigateBack = { navController.safePopBackStack() },
                    bottomContentPadding = floatingNavigationContentBottomPadding,
                )
            }
            composable<AboutRoute> {
                AboutScreen(navigateBack = { navController.safePopBackStack() })
            }
        }
    }

    when (navigationLayout) {
        CuratedNavigationLayout.ModalDrawer -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = navigationDrawerEnabled,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier =
                            Modifier.width(
                                curatedNavigationDrawerWidth(CuratedNavigationLayout.ModalDrawer)
                            )
                    ) {
                        CuratedNavigationDrawerContent(
                            navigationItems = drawerNavigationItems,
                            selectedRoute = selectedNavigationRoute,
                            onNavigationItemClick = onNavigationItemClick,
                        )
                    }
                },
            ) {
                CuratedNavigationContentWithFloatingBar(
                    showFloatingBar = bottomNavigationVisible,
                    navigationItems = bottomNavigationItems,
                    selectedRoute = selectedNavigationRoute,
                    safeDrawingBottom = safeDrawingBottom,
                    onNavigationItemClick = onNavigationItemClick,
                    content = navigationContent,
                )
            }
        }
        CuratedNavigationLayout.PermanentDrawer -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier =
                            Modifier.width(
                                curatedNavigationDrawerWidth(
                                    CuratedNavigationLayout.PermanentDrawer
                                )
                            )
                    ) {
                        CuratedNavigationDrawerContent(
                            navigationItems = drawerNavigationItems,
                            selectedRoute = selectedNavigationRoute,
                            onNavigationItemClick = onNavigationItemClick,
                        )
                    }
                }
            ) {
                CuratedNavigationContentWithFloatingBar(
                    showFloatingBar = bottomNavigationVisible,
                    navigationItems = bottomNavigationItems,
                    selectedRoute = selectedNavigationRoute,
                    safeDrawingBottom = safeDrawingBottom,
                    onNavigationItemClick = onNavigationItemClick,
                    content = navigationContent,
                )
            }
        }
    }
}

@Composable
private fun CuratedNavigationContentWithFloatingBar(
    showFloatingBar: Boolean,
    navigationItems: List<TabBarItem>,
    selectedRoute: String?,
    safeDrawingBottom: androidx.compose.ui.unit.Dp,
    onNavigationItemClick: (TabBarItem) -> Unit,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (showFloatingBar) {
            CuratedFloatingBottomNavigationBar(
                navigationItems = navigationItems,
                selectedRoute = selectedRoute,
                onNavigationItemClick = onNavigationItemClick,
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = safeDrawingBottom + CuratedFloatingNavigationBarBottomMargin,
                        )
                        .widthIn(max = curatedFloatingNavigationBarMaxWidth())
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CuratedFloatingBottomNavigationBar(
    navigationItems: List<TabBarItem>,
    selectedRoute: String?,
    onNavigationItemClick: (TabBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = curatedFloatingNavigationColors()

    Surface(
        modifier = modifier,
        shape = colors.shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.borderColor),
    ) {
        Row(
            modifier =
                Modifier.height(CuratedFloatingNavigationBarHeight)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationItems.forEach { item ->
                CuratedFloatingBottomNavigationItem(
                    item = item,
                    selected = selectedRoute == item.route::class.qualifiedName,
                    colors = colors,
                    onClick = { onNavigationItemClick(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CuratedFloatingBottomNavigationItem(
    item: TabBarItem,
    selected: Boolean,
    colors: CuratedFloatingNavigationColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(CuratedFloatingNavigationItemHeight),
        shape = colors.shape,
        color = if (selected) colors.selectedContainerColor else Color.Transparent,
        contentColor = if (selected) colors.selectedContentColor else colors.unselectedContentColor,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = stringResource(item.title),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

private data class CuratedFloatingNavigationColors(
    val containerColor: Color,
    val contentColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val selectedContainerColor: Color,
    val borderColor: Color,
    val shape: Shape,
)

@Composable
private fun curatedFloatingNavigationColors(): CuratedFloatingNavigationColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val contentColor = colorScheme.onSurface
    val containerColor =
        curatedFloatingNavigationContainerBaseColor(colorScheme.surfaceContainerHigh)
            .copy(alpha = curatedFloatingNavigationContainerAlpha(isDarkTheme))

    return CuratedFloatingNavigationColors(
        containerColor = containerColor,
        contentColor = contentColor,
        selectedContentColor = contentColor,
        unselectedContentColor = contentColor.copy(alpha = 0.68f),
        selectedContainerColor =
            colorScheme.primary.copy(
                alpha = curatedFloatingNavigationSelectedContainerAlpha(isDarkTheme)
            ),
        borderColor = colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.30f else 0.58f),
        shape = CircleShape,
    )
}

data class CuratedNavigationItemColorSpec(
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val selectedIndicatorColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
)

internal fun curatedNavigationItemColorSpec(
    selectedContentColor: Color,
    unselectedContentColor: Color,
    selectedIndicatorColor: Color,
): CuratedNavigationItemColorSpec =
    CuratedNavigationItemColorSpec(
        selectedIconColor = selectedContentColor,
        selectedTextColor = selectedContentColor,
        selectedIndicatorColor = selectedIndicatorColor,
        unselectedIconColor = unselectedContentColor,
        unselectedTextColor = unselectedContentColor,
    )

@Composable
private fun CuratedNavigationDrawerContent(
    navigationItems: List<TabBarItem>,
    selectedRoute: String?,
    onNavigationItemClick: (TabBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemColors = curatedNavigationDrawerItemColors()

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        navigationItems.forEach { item ->
            NavigationDrawerItem(
                selected = selectedRoute == item.route::class.qualifiedName,
                onClick = { onNavigationItemClick(item) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                    )
                },
                label = { Text(text = stringResource(item.title)) },
                colors = itemColors,
            )
        }
    }
}

@Composable
private fun curatedNavigationDrawerItemColors() =
    navigationItemColors().let { colors ->
        NavigationDrawerItemDefaults.colors(
            selectedIconColor = colors.selectedIconColor,
            selectedTextColor = colors.selectedTextColor,
            selectedContainerColor = colors.selectedIndicatorColor,
            unselectedIconColor = colors.unselectedIconColor,
            unselectedTextColor = colors.unselectedTextColor,
        )
    }

@Composable
private fun navigationItemColors(): CuratedNavigationItemColorSpec {
    val colors =
        curatedNavigationItemColorSpec(
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )

    return colors
}

internal fun curatedStartDestination(
    hasServers: Boolean,
    hasCurrentServer: Boolean,
    hasCurrentUser: Boolean,
    isCuratedAuthLocked: Boolean = false,
): Any =
    when {
        hasServers && hasCurrentServer && isCuratedAuthLocked -> AuthLockRoute
        hasServers && hasCurrentServer -> HomeRoute
        hasServers -> ServersRoute
        else -> WelcomeRoute
    }

internal fun curatedNavigationItems(isOfflineMode: Boolean): List<TabBarItem> =
    curatedBottomNavigationItems() + curatedDrawerNavigationItems(isOfflineMode)

internal fun curatedBottomNavigationItems(): List<TabBarItem> = listOf(homeTab, mediaTab, settingsTab)

internal fun curatedDrawerNavigationItems(isOfflineMode: Boolean): List<TabBarItem> =
    listOf(actorsTab, historyTab)

internal fun curatedFloatingNavigationBarVisible(currentRoute: String?): Boolean =
    currentRoute in
        setOf(
            HomeRoute::class.qualifiedName,
            MediaRoute::class.qualifiedName,
            ActorsRoute::class.qualifiedName,
            HistoryRoute::class.qualifiedName,
            SettingsRoute::class.qualifiedName,
        )

internal fun curatedFloatingNavigationContentBottomPadding(safeDrawingBottom: androidx.compose.ui.unit.Dp) =
    safeDrawingBottom +
        CuratedFloatingNavigationBarHeight +
        CuratedFloatingNavigationBarBottomMargin +
        CuratedFloatingNavigationContentExtraScrollClearance

internal fun curatedFloatingNavigationBarHeight() = CuratedFloatingNavigationBarHeight

internal fun curatedFloatingNavigationItemHeight() = CuratedFloatingNavigationItemHeight

internal fun curatedFloatingNavigationBarMaxWidth() = CuratedFloatingNavigationBarMaxWidth

internal fun curatedFloatingNavigationContainerBaseColor(surfaceContainerHigh: Color) =
    surfaceContainerHigh

internal fun curatedFloatingNavigationContainerAlpha(isDarkTheme: Boolean) =
    if (isDarkTheme) {
        0.94f
    } else {
        0.96f
    }

internal fun curatedFloatingNavigationSelectedContainerAlpha(isDarkTheme: Boolean) =
    if (isDarkTheme) {
        0.20f
    } else {
        0.16f
    }

enum class CuratedNavigationLayout {
    ModalDrawer,
    PermanentDrawer,
}

internal fun curatedNavigationDrawerWidth(layout: CuratedNavigationLayout) =
    when (layout) {
        CuratedNavigationLayout.ModalDrawer -> 256.dp
        CuratedNavigationLayout.PermanentDrawer -> 224.dp
    }

internal fun curatedNavigationLayoutType(isExpandedWidth: Boolean): CuratedNavigationLayout =
    if (isExpandedWidth) {
        CuratedNavigationLayout.PermanentDrawer
    } else {
        CuratedNavigationLayout.ModalDrawer
    }

internal fun curatedNavigationSelectedRoute(currentRoute: String?): String? =
    when (currentRoute) {
        MovieRoute::class.qualifiedName,
        LibraryRoute::class.qualifiedName,
        CollectionRoute::class.qualifiedName,
        FavoritesRoute::class.qualifiedName,
        DownloadsRoute::class.qualifiedName,
        ShowRoute::class.qualifiedName,
        EpisodeRoute::class.qualifiedName,
        SeasonRoute::class.qualifiedName,
        PersonRoute::class.qualifiedName -> MediaRoute::class.qualifiedName
        ActorRoute::class.qualifiedName -> ActorsRoute::class.qualifiedName
        SettingsRoute::class.qualifiedName,
        AboutRoute::class.qualifiedName -> SettingsRoute::class.qualifiedName
        else -> currentRoute
    }

internal fun curatedNavigationDrawerEnabled(
    selectedRoute: String?,
    navigationItems: List<TabBarItem>,
): Boolean = selectedRoute in navigationItems.map { it.route::class.qualifiedName }

internal fun isCuratedVisibleItem(item: FindroidItem): Boolean =
    when (item) {
        is FindroidCollection -> isCuratedVisibleCollectionType(item.type)
        is FindroidShow,
        is FindroidSeason,
        is FindroidEpisode -> false
        else -> true
    }

internal fun isCuratedVisibleCollectionType(type: CollectionType): Boolean =
    type != CollectionType.TvShows

internal fun curatedRouteForItem(item: FindroidItem): Any? =
    when (item) {
        is FindroidBoxSet ->
            CollectionRoute(collectionId = item.id.toString(), collectionName = item.name)
        is FindroidMovie -> MovieRoute(movieId = item.id.toString())
        is FindroidCollection ->
            if (isCuratedVisibleCollectionType(item.type)) {
                LibraryRoute(
                    libraryId = item.id.toString(),
                    libraryName = item.name,
                    libraryType = item.type,
                )
            } else {
                null
            }
        is FindroidFolder ->
            LibraryRoute(
                libraryId = item.id.toString(),
                libraryName = item.name,
                libraryType = CollectionType.Folders,
            )
        else -> null
    }

internal fun curatedMovieIdForRoute(route: MovieRoute): String = route.movieId

private fun navigateHome(navController: NavHostController) {
    navController.safeNavigate(HomeRoute) {
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
}

private fun navigateHomeAndClearBackStack(navController: NavHostController) {
    navController.safeNavigate(HomeRoute) {
        popUpTo(0)
        launchSingleTop = true
    }
}

private fun navigateAuthGate(navController: NavHostController) {
    navController.safeNavigate(AuthLockRoute) {
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
}

private fun navigateToItem(navController: NavHostController, item: FindroidItem) {
    curatedRouteForItem(item)?.let { navController.safeNavigate(it) }
}

private fun <T : Any> NavHostController.safeNavigate(
    route: T,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.navigate(route, navOptions, navigatorExtras)
    }
}

private fun <T : Any> NavHostController.safeNavigate(
    route: T,
    builder: NavOptionsBuilder.() -> Unit,
) {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.navigate(route, builder)
    }
}

private fun NavHostController.safePopBackStack(): Boolean {
    return if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    } else {
        false
    }
}
