package dev.jdtech.jellyfin

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.models.CollectionType
import dev.jdtech.jellyfin.models.FindroidBoxSet
import dev.jdtech.jellyfin.models.FindroidCollection
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.models.FindroidFolder
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.FindroidMovie
import dev.jdtech.jellyfin.models.FindroidSeason
import dev.jdtech.jellyfin.models.FindroidShow
import dev.jdtech.jellyfin.presentation.film.CollectionScreen
import dev.jdtech.jellyfin.presentation.film.DownloadsScreen
import dev.jdtech.jellyfin.presentation.film.EpisodeScreen
import dev.jdtech.jellyfin.presentation.film.FavoritesScreen
import dev.jdtech.jellyfin.presentation.film.HomeScreen
import dev.jdtech.jellyfin.presentation.film.LibraryScreen
import dev.jdtech.jellyfin.presentation.film.MediaScreen
import dev.jdtech.jellyfin.presentation.film.MovieScreen
import dev.jdtech.jellyfin.presentation.film.PersonScreen
import dev.jdtech.jellyfin.presentation.film.SeasonScreen
import dev.jdtech.jellyfin.presentation.film.ShowScreen
import dev.jdtech.jellyfin.presentation.curated.CuratedActorDetailScreen
import dev.jdtech.jellyfin.presentation.curated.CuratedActorsScreen
import dev.jdtech.jellyfin.presentation.curated.CuratedHistoryScreen
import dev.jdtech.jellyfin.presentation.curated.CuratedMovieDetailScreen
import dev.jdtech.jellyfin.presentation.curated.CuratedMoviesScreen
import dev.jdtech.jellyfin.presentation.settings.AboutScreen
import dev.jdtech.jellyfin.presentation.settings.SettingsScreen
import dev.jdtech.jellyfin.presentation.setup.addresses.ServerAddressesScreen
import dev.jdtech.jellyfin.presentation.setup.addserver.AddServerScreen
import dev.jdtech.jellyfin.presentation.setup.authlock.AuthLockScreen
import dev.jdtech.jellyfin.presentation.setup.login.LoginScreen
import dev.jdtech.jellyfin.presentation.setup.servers.ServersScreen
import dev.jdtech.jellyfin.presentation.setup.users.UsersScreen
import dev.jdtech.jellyfin.presentation.setup.welcome.WelcomeScreen
import dev.jdtech.jellyfin.presentation.utils.LocalOfflineMode
import java.util.UUID
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
val downloadsTab =
    TabBarItem(
        title = CoreR.string.title_download,
        icon = CoreR.drawable.ic_download,
        route = DownloadsRoute,
    )

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

    val navigationItems = curatedNavigationItems(isOfflineMode)
    val navigationItemClassNames = navigationItems.map { it.route::class.qualifiedName }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var searchExpanded by remember { mutableStateOf(false) }

    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in navigationItemClassNames && !searchExpanded

    val navigationSuiteScaffoldState = rememberNavigationSuiteScaffoldState()

    LaunchedEffect(showBottomBar) {
        if (showBottomBar) {
            navigationSuiteScaffoldState.show()
        } else {
            navigationSuiteScaffoldState.hide()
        }
    }

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType =
        with(windowAdaptiveInfo) {
            if (
                windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                )
            ) {
                NavigationSuiteType.NavigationRail
            } else {
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(this)
            }
        }
    val navigationItemColors = curatedNavigationItemColors()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navigationItems.forEach { item ->
                item(
                    selected = currentRoute == item.route::class.qualifiedName,
                    onClick = {
                        searchExpanded = false

                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = stringResource(item.title),
                        )
                    },
                    enabled = item.enabled,
                    label = { Text(text = stringResource(item.title)) },
                    colors = navigationItemColors,
                )
            }
        },
        layoutType = customNavSuiteType,
        state = navigationSuiteScaffoldState,
    ) {
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
                CuratedMoviesScreen(
                    onMovieClick = { movieId -> navController.safeNavigate(MovieRoute(movieId)) },
                    onSettingsClick = {
                        navController.safeNavigate(
                            SettingsRoute(indexes = intArrayOf(CoreR.string.title_settings))
                        )
                    },
                )
            }
            composable<MediaRoute> {
                CuratedMoviesScreen(
                    onMovieClick = { movieId -> navController.safeNavigate(MovieRoute(movieId)) },
                    onSettingsClick = {
                        navController.safeNavigate(
                            SettingsRoute(indexes = intArrayOf(CoreR.string.title_settings))
                        )
                    },
                )
            }
            composable<ActorsRoute> {
                CuratedActorsScreen(
                    onActorClick = { actorName -> navController.safeNavigate(ActorRoute(actorName)) }
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
                    }
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
                    navigateHome = { navigateHome(navController) },
                    onPlayMovie = { movieId, title ->
                        context.startActivity(
                            Intent(context, CuratedPlayerActivity::class.java).apply {
                                putExtra(CuratedPlayerContract.EXTRA_MOVIE_ID, movieId)
                                putExtra(CuratedPlayerContract.EXTRA_TITLE, title)
                            }
                        )
                    },
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
                )
            }
            composable<AboutRoute> {
                AboutScreen(navigateBack = { navController.safePopBackStack() })
            }
        }
    }
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
private fun curatedNavigationItemColors(): NavigationSuiteItemColors {
    val colors =
        curatedNavigationItemColorSpec(
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )

    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = colors.selectedIconColor,
                selectedTextColor = colors.selectedTextColor,
                indicatorColor = colors.selectedIndicatorColor,
                unselectedIconColor = colors.unselectedIconColor,
                unselectedTextColor = colors.unselectedTextColor,
            ),
        navigationRailItemColors =
            NavigationRailItemDefaults.colors(
                selectedIconColor = colors.selectedIconColor,
                selectedTextColor = colors.selectedTextColor,
                indicatorColor = colors.selectedIndicatorColor,
                unselectedIconColor = colors.unselectedIconColor,
                unselectedTextColor = colors.unselectedTextColor,
            ),
        navigationDrawerItemColors =
            NavigationDrawerItemDefaults.colors(
                selectedIconColor = colors.selectedIconColor,
                selectedTextColor = colors.selectedTextColor,
                selectedContainerColor = colors.selectedIndicatorColor,
                unselectedIconColor = colors.unselectedIconColor,
                unselectedTextColor = colors.unselectedTextColor,
            ),
    )
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
    listOf(homeTab, mediaTab, actorsTab, historyTab)

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
