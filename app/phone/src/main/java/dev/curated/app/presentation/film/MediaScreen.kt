package dev.curated.app.presentation.film

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import dev.curated.app.isCuratedVisibleCollectionType
import dev.curated.app.isCuratedVisibleItem
import dev.curated.app.core.presentation.dummy.dummyCollections
import dev.curated.app.film.presentation.media.MediaAction
import dev.curated.app.film.presentation.media.MediaState
import dev.curated.app.film.presentation.media.MediaViewModel
import dev.curated.app.film.presentation.search.SearchAction
import dev.curated.app.film.presentation.search.SearchState
import dev.curated.app.film.presentation.search.SearchViewModel
import dev.curated.app.models.FindroidItem
import dev.curated.app.presentation.components.ErrorDialog
import dev.curated.app.presentation.film.components.Direction
import dev.curated.app.presentation.film.components.ErrorCard
import dev.curated.app.presentation.film.components.FavoritesCard
import dev.curated.app.presentation.film.components.FilmSearchBar
import dev.curated.app.presentation.film.components.ItemCard
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings
import dev.curated.app.presentation.utils.rememberSafePadding

@Composable
fun MediaScreen(
    onItemClick: (FindroidItem) -> Unit,
    onFavoritesClick: () -> Unit,
    searchExpanded: Boolean,
    onSearchExpand: (Boolean) -> Unit,
    viewModel: MediaViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchState by searchViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadData() }

    MediaScreenLayout(
        state = state,
        searchState = searchState,
        searchExpanded = searchExpanded,
        onSearchExpand = onSearchExpand,
        onAction = { action ->
            when (action) {
                is MediaAction.OnItemClick -> onItemClick(action.item)
                is MediaAction.OnFavoritesClick -> onFavoritesClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        onSearchAction = { action ->
            when (action) {
                is SearchAction.OnItemClick -> onItemClick(action.item)
                else -> Unit
            }
            searchViewModel.onAction(action)
        },
    )
}

@Composable
private fun MediaScreenLayout(
    state: MediaState,
    searchState: SearchState,
    searchExpanded: Boolean,
    onSearchExpand: (Boolean) -> Unit,
    onAction: (MediaAction) -> Unit,
    onSearchAction: (SearchAction) -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    val paddingStart = safePadding.start + MaterialTheme.spacings.default
    val paddingEnd = safePadding.end + MaterialTheme.spacings.default
    val paddingBottom = safePadding.bottom + MaterialTheme.spacings.default

    val contentPaddingTop by
        animateDpAsState(
            targetValue =
                if (state.error != null) {
                    safePadding.top + 144.dp
                } else {
                    safePadding.top + 88.dp
                },
            label = "content_padding",
        )

    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    val visibleLibraries = state.libraries.filter { isCuratedVisibleCollectionType(it.type) }
    val visibleSearchState =
        searchState.copy(items = searchState.items.filter(::isCuratedVisibleItem))

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val minColumnSize =
        when {
            windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
            ) -> 320.dp
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                240.dp
            else -> 160.dp
        }

    Box(modifier = Modifier.fillMaxSize()) {
        FilmSearchBar(
            state = visibleSearchState,
            expanded = searchExpanded,
            onExpand = onSearchExpand,
            onAction = onSearchAction,
            modifier = Modifier.fillMaxWidth(),
            paddingStart = paddingStart,
            paddingEnd = paddingEnd,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minColumnSize),
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = paddingStart,
                    top = contentPaddingTop,
                    end = paddingEnd,
                    bottom = paddingBottom,
                ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FavoritesCard(onClick = { onAction(MediaAction.OnFavoritesClick) })
            }
            items(visibleLibraries, key = { it.id }) { library ->
                ItemCard(
                    item = library,
                    direction = Direction.HORIZONTAL,
                    onClick = { onAction(MediaAction.OnItemClick(library)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        if (state.error != null) {
            ErrorCard(
                onShowStacktrace = { showErrorDialog = true },
                onRetryClick = { onAction(MediaAction.OnRetryClick) },
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            start = paddingStart,
                            top = safePadding.top + 80.dp,
                            end = paddingEnd,
                        ),
            )
            if (showErrorDialog) {
                ErrorDialog(
                    exception = state.error!!,
                    onDismissRequest = { showErrorDialog = false },
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun MediaScreenLayoutPreview() {
    CuratedTheme {
        MediaScreenLayout(
            state =
                MediaState(libraries = dummyCollections, error = Exception("Failed to load data")),
            searchState = SearchState(),
            searchExpanded = false,
            onSearchExpand = {},
            onAction = {},
            onSearchAction = {},
        )
    }
}
