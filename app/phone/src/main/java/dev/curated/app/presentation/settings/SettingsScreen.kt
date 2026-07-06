package dev.curated.app.presentation.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.curated.CuratedNavigationMenuButton
import dev.curated.app.presentation.settings.components.SettingsGroupCard
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings
import dev.curated.app.presentation.utils.rememberSafePadding
import dev.curated.app.settings.R as SettingsR
import dev.curated.app.settings.presentation.enums.DeviceType
import dev.curated.app.settings.presentation.models.PreferenceCategory
import dev.curated.app.settings.presentation.models.PreferenceGroup
import dev.curated.app.settings.presentation.settings.SettingsAction
import dev.curated.app.settings.presentation.settings.SettingsEvent
import dev.curated.app.settings.presentation.settings.SettingsState
import dev.curated.app.settings.presentation.settings.SettingsViewModel
import dev.curated.app.utils.ObserveAsEvents
import dev.curated.app.utils.restart
import timber.log.Timber

@Composable
fun SettingsScreen(
    indexes: IntArray = intArrayOf(),
    navigateToSettings: (indexes: IntArray) -> Unit,
    navigateToServers: () -> Unit,
    navigateToUsers: () -> Unit,
    navigateToAbout: () -> Unit,
    onOpenNavigation: (() -> Unit)? = null,
    navigateBack: () -> Unit,
    bottomContentPadding: Dp = 16.dp,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadPreferences(indexes, DeviceType.PHONE) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.NavigateToSettings -> navigateToSettings(event.indexes)
            is SettingsEvent.NavigateToUsers -> navigateToUsers()
            is SettingsEvent.NavigateToServers -> navigateToServers()
            is SettingsEvent.NavigateToAbout -> navigateToAbout()
            is SettingsEvent.LaunchIntent -> {
                try {
                    context.startActivity(event.intent)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
            is SettingsEvent.RestartActivity -> {
                try {
                    (context as Activity).restart()
                } catch (_: Exception) {}
            }
        }
    }

    SettingsScreenLayout(
        state = state,
        onAction = { action ->
            when (action) {
                is SettingsAction.OnBackClick -> navigateBack()
                is SettingsAction.OnUpdate -> {
                    viewModel.onAction(action)
                    viewModel.loadPreferences(indexes, DeviceType.PHONE)
                }
            }
        },
        isRootRoute = isSettingsRootRoute(indexes),
        onOpenNavigation = onOpenNavigation,
        bottomContentPadding = bottomContentPadding,
    )
}

@Composable
private fun SettingsScreenLayout(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    isRootRoute: Boolean = true,
    onOpenNavigation: (() -> Unit)? = null,
    bottomContentPadding: Dp = 16.dp,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)
    val contentPadding =
        PaddingValues(
            start = MaterialTheme.spacings.default,
            top = MaterialTheme.spacings.small,
            end = MaterialTheme.spacings.default,
            bottom = bottomContentPadding,
        )

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(
            isRootRoute = isRootRoute,
            onOpenNavigation = onOpenNavigation,
            onBackClick = { onAction(SettingsAction.OnBackClick) },
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacings.default,
                        top = curatedSettingsHeaderTopPadding(safePadding.top),
                        end = MaterialTheme.spacings.default,
                        bottom = MaterialTheme.spacings.small,
                    ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(state.preferenceGroups) { group ->
                SettingsGroupCard(
                    group = group,
                    onAction = onAction,
                    modifier = Modifier.widthIn(max = 640.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    isRootRoute: Boolean,
    onOpenNavigation: (() -> Unit)?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (isRootRoute) {
            onOpenNavigation?.let { CuratedNavigationMenuButton(onClick = it) }
        } else {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_left),
                    contentDescription = null,
                )
            }
        }
    }
}

internal fun isSettingsRootRoute(indexes: IntArray): Boolean =
    indexes.isEmpty() || indexes.contentEquals(intArrayOf(CoreR.string.title_settings))

internal fun curatedSettingsHeaderTopPadding(safeDrawingTop: Dp): Dp = safeDrawingTop + 8.dp

@PreviewScreenSizes
@Composable
private fun SettingsScreenLayoutPreview() {
    CuratedTheme {
        SettingsScreenLayout(
            state =
                SettingsState(
                    preferenceGroups =
                        listOf(
                            PreferenceGroup(
                                nameStringResource = null,
                                preferences =
                                    listOf(
                                        PreferenceCategory(
                                            nameStringResource =
                                                SettingsR.string.settings_category_language,
                                            iconDrawableId = SettingsR.drawable.ic_languages,
                                        )
                                    ),
                            ),
                            PreferenceGroup(
                                nameStringResource = null,
                                preferences =
                                    listOf(
                                        PreferenceCategory(
                                            nameStringResource =
                                                SettingsR.string.settings_category_interface,
                                            iconDrawableId = SettingsR.drawable.ic_palette,
                                        )
                                    ),
                            ),
                        )
                ),
            onAction = {},
        )
    }
}
