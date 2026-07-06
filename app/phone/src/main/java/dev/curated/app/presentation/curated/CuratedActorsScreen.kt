package dev.curated.app.presentation.curated

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.curated.app.core.R as CoreR
import dev.curated.app.curated.api.ActorListItem
import dev.curated.app.presentation.utils.GridCellsAdaptiveWithMinColumns
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CuratedActorsScreen(
    onOpenNavigation: (() -> Unit)? = null,
    onActorClick: (String) -> Unit,
    bottomContentPadding: Dp = 16.dp,
    viewModel: CuratedActorsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CuratedActorsLayout(
        state = state,
        onActorClick = onActorClick,
        onRetryClick = viewModel::loadActors,
        onLoadMore = viewModel::loadNextPage,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onOpenNavigation = onOpenNavigation,
        bottomContentPadding = bottomContentPadding,
    )
}

@Composable
private fun CuratedActorsLayout(
    state: CuratedActorsState,
    onActorClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenNavigation: (() -> Unit)?,
    bottomContentPadding: Dp,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, state.actors.size, state.canLoadMore, state.appendErrorMessage) {
        snapshotFlow {
                val layoutInfo = gridState.layoutInfo
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
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
        CuratedActorsHeader(
            state = state,
            onSearchQueryChange = onSearchQueryChange,
            onOpenNavigation = onOpenNavigation,
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                CuratedActorsErrorState(message = state.errorMessage, onRetryClick = onRetryClick)
            }
            state.actors.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = curatedActorsSearchEmptyMessage(state.searchQuery))
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCellsAdaptiveWithMinColumns(minSize = 132.dp, minColumns = 2),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding =
                        PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomContentPadding),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.actors, key = { it.name }) { actor ->
                        CuratedActorCard(
                            actor = actor,
                            onClick = { onActorClick(actor.name) },
                        )
                    }
                    if (state.isLoadingMore || state.appendErrorMessage != null) {
                        item(
                            key = "curated-actors-load-more",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            CuratedActorsLoadMoreFooter(
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
private fun CuratedActorsHeader(
    state: CuratedActorsState,
    onSearchQueryChange: (String) -> Unit,
    onOpenNavigation: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var searchActive by rememberSaveable { mutableStateOf(false) }
    val showSearch = searchActive || state.searchQuery.isNotBlank()

    LaunchedEffect(searchActive) {
        if (searchActive) {
            focusRequester.requestFocus()
        }
    }

    CuratedPageHeader(modifier = modifier) {
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
                    contentDescription = "Close search",
                )
            }
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(text = "Search actors") },
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
                                contentDescription = "Clear search",
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
            )
        } else {
            CuratedPageHeaderTitle(
                text = stringResource(CoreR.string.title_actors),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { searchActive = true }) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_search),
                    contentDescription = "Search",
                )
            }
            onOpenNavigation?.let { CuratedNavigationMenuButton(onClick = it) }
        }
    }
}

@Composable
private fun CuratedActorCard(actor: ActorListItem, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick) {
        CuratedActorAvatar(
            imageUrl = actor.avatarUrl,
            contentDescription = actor.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = actor.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = curatedActorMovieCountText(actor.movieCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val tags = actor.userTags.take(2).joinToString(", ")
            if (tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tags,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun CuratedActorAvatar(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_user),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

internal fun curatedActorMovieCountText(movieCount: Int): String =
    if (movieCount == 1) "1 movie" else "$movieCount movies"

private fun curatedActorsSearchEmptyMessage(searchQuery: String): String =
    if (curatedActorsNormalizedSearchQuery(searchQuery) == null) {
        "No actors found"
    } else {
        "No matching actors"
    }

@Composable
private fun CuratedActorsLoadMoreFooter(
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
                Button(onClick = onRetryClick) { Text(stringResource(CoreR.string.retry)) }
            }
        }
    }
}

@Composable
private fun CuratedActorsErrorState(message: String, onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetryClick) { Text(stringResource(CoreR.string.retry)) }
    }
}
