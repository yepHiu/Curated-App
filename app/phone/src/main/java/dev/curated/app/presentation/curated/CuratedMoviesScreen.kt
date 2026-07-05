package dev.curated.app.presentation.curated

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import dev.curated.app.core.R as CoreR
import dev.curated.app.curated.api.MovieListItem
import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.presentation.utils.GridCellsAdaptiveWithMinColumns
import dev.curated.app.presentation.utils.rememberSafePadding
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CuratedMoviesScreen(
    onOpenNavigation: (() -> Unit)? = null,
    onMovieClick: (String) -> Unit,
    viewModel: CuratedMoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshPlaybackProgress()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CuratedMoviesLayout(
        state = state,
        onMovieClick = onMovieClick,
        onRetryClick = viewModel::loadMovies,
        onLoadMore = viewModel::loadNextPage,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onOpenNavigation = onOpenNavigation,
    )
}

@Composable
private fun CuratedMoviesLayout(
    state: CuratedMoviesState,
    onMovieClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenNavigation: (() -> Unit)?,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, state.movies.size, state.canLoadMore, state.appendErrorMessage) {
        snapshotFlow {
                val layoutInfo = gridState.layoutInfo
                val lastVisibleItemIndex =
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                curatedMoviesShouldRequestNextPage(
                    lastVisibleItemIndex = lastVisibleItemIndex,
                    totalItemCount = layoutInfo.totalItemsCount,
                    canLoadMore = state.canLoadMore && state.appendErrorMessage == null,
                )
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    onLoadMore()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CuratedMoviesHeader(
            state = state,
            onSearchQueryChange = onSearchQueryChange,
            onOpenNavigation = onOpenNavigation,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = curatedMoviesHeaderTopPadding(safePadding.top),
                        end = 16.dp,
                        bottom = 8.dp,
                    ),
        )

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                CuratedErrorState(message = state.errorMessage, onRetryClick = onRetryClick)
            }
            state.movies.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = curatedMoviesSearchEmptyMessage(state.searchQuery))
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCellsAdaptiveWithMinColumns(minSize = 150.dp, minColumns = 2),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.movies, key = { it.id }) { movie ->
                        CuratedMovieCard(
                            movie = movie,
                            progress = state.playbackProgressByMovieId[movie.id],
                            onClick = { onMovieClick(movie.id) },
                        )
                    }
                    if (state.isLoadingMore || state.appendErrorMessage != null) {
                        item(
                            key = "curated-movies-load-more",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            CuratedMoviesLoadMoreFooter(
                                isLoading = state.isLoadingMore,
                                errorMessage = state.appendErrorMessage,
                                onRetryClick = onLoadMore,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedMoviesHeader(
    state: CuratedMoviesState,
    onSearchQueryChange: (String) -> Unit,
    onOpenNavigation: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var searchActive by rememberSaveable { mutableStateOf(false) }
    val showSearch = searchActive || state.searchQuery.isNotBlank()
    val actionContentDescriptions = curatedMoviesHeaderActionContentDescriptions()

    LaunchedEffect(searchActive) {
        if (searchActive) {
            focusRequester.requestFocus()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (showSearch) {
            IconButton(
                onClick = {
                    searchActive = false
                    focusManager.clearFocus()
                    onSearchQueryChange("")
                }
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_left),
                    contentDescription = curatedMoviesSearchCloseContentDescription(),
                )
            }
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(text = curatedMoviesSearchPlaceholder()) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                painter = painterResource(CoreR.drawable.ic_x),
                                contentDescription = curatedMoviesSearchClearContentDescription(),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
            )
        } else {
            onOpenNavigation?.let { CuratedNavigationMenuButton(onClick = it) }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { searchActive = true }) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_search),
                    contentDescription = actionContentDescriptions[0],
                )
            }
        }
    }
}

@Composable
private fun CuratedMovieCard(movie: MovieListItem, progress: PlaybackProgress?, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick) {
        val posterUrl = curatedMovieCardImageUrl(movie)
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(0.68f),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.fillMaxWidth()
                        .aspectRatio(0.68f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_film),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        curatedMovieCardProgressFraction(progress)?.let { progressFraction ->
            CuratedMovieCardProgressBar(progress = progressFraction)
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val metadata =
                listOf(movie.code, movie.studio, movie.year.takeIf { it > 0 }?.toString())
                    .filterNotNull()
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            Text(
                text = metadata.ifBlank { movie.resolution },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CuratedMovieCardProgressBar(progress: Float) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(3.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier =
                Modifier.fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary)
        )
    }
}

internal fun curatedMovieCardImageUrl(movie: MovieListItem): String? =
    movie.thumbUrl ?: movie.coverUrl

internal fun curatedMovieCardProgressFraction(progress: PlaybackProgress?): Float? {
    val durationSec = progress?.durationSec?.takeIf { it > 0.0 } ?: return null
    if (progress.positionSec <= 0.0) return null
    return (progress.positionSec / durationSec).toFloat().coerceIn(0f, 1f)
}

internal fun curatedMoviesHeaderTopPadding(safeDrawingTop: Dp): Dp = safeDrawingTop + 8.dp

internal fun curatedMoviesHeaderSubtitle(total: Int): String = "Movie library"

internal fun curatedMoviesHeaderActionContentDescriptions(): List<String> =
    listOf("Search")

internal fun curatedMoviesSearchPlaceholder(): String = "Search movies"

internal fun curatedMoviesSearchCloseContentDescription(): String = "Close search"

internal fun curatedMoviesSearchClearContentDescription(): String = "Clear search"

internal fun curatedMoviesSearchEmptyMessage(searchQuery: String): String =
    if (curatedMoviesNormalizedSearchQuery(searchQuery) == null) {
        "No movies found"
    } else {
        "No matching movies"
    }

internal fun curatedMoviesShouldRequestNextPage(
    lastVisibleItemIndex: Int,
    totalItemCount: Int,
    canLoadMore: Boolean,
    loadAheadItemCount: Int = 6,
): Boolean =
    canLoadMore &&
        totalItemCount > 0 &&
        lastVisibleItemIndex >= 0 &&
        lastVisibleItemIndex >= totalItemCount - loadAheadItemCount

@Composable
private fun CuratedMoviesLoadMoreFooter(
    isLoading: Boolean,
    errorMessage: String?,
    onRetryClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else if (errorMessage != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetryClick) { Text("Retry") }
            }
        }
    }
}

@Composable
private fun CuratedErrorState(message: String, onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetryClick) { Text("Retry") }
    }
}
