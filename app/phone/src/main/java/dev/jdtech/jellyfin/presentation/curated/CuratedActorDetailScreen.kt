package dev.jdtech.jellyfin.presentation.curated

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.curated.api.ActorProfile
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.presentation.utils.GridCellsAdaptiveWithMinColumns
import dev.jdtech.jellyfin.presentation.utils.rememberSafePadding
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CuratedActorDetailScreen(
    actorName: String,
    navigateBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    viewModel: CuratedActorDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(actorName) { viewModel.loadActor(actorName) }

    CuratedActorDetailLayout(
        actorName = actorName,
        state = state,
        navigateBack = navigateBack,
        onMovieClick = onMovieClick,
        onRetryClick = { viewModel.loadActor(actorName) },
        onLoadMore = viewModel::loadNextMovies,
    )
}

@Composable
private fun CuratedActorDetailLayout(
    actorName: String,
    state: CuratedActorDetailState,
    navigateBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        top = safePadding.top + 8.dp,
                        end = 8.dp,
                        bottom = 8.dp,
                    ),
        ) {
            IconButton(onClick = navigateBack) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_left),
                    contentDescription = "Back",
                )
            }
            Text(
                text = state.profile?.name ?: actorName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                CuratedActorDetailErrorState(
                    message = state.errorMessage,
                    onRetryClick = onRetryClick,
                )
            }
            state.profile != null -> {
                CuratedActorDetailContent(
                    profile = state.profile,
                    movies = state.movies,
                    onMovieClick = onMovieClick,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun CuratedActorDetailContent(
    profile: ActorProfile,
    movies: CuratedActorMoviesState,
    onMovieClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, movies.movies.size, movies.canLoadMore, movies.appendErrorMessage) {
        snapshotFlow {
                val layoutInfo = gridState.layoutInfo
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                curatedMoviesShouldRequestNextPage(
                    lastVisibleItemIndex = lastVisibleItemIndex,
                    totalItemCount = layoutInfo.totalItemsCount,
                    canLoadMore = movies.canLoadMore && movies.appendErrorMessage == null,
                )
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCellsAdaptiveWithMinColumns(minSize = 150.dp, minColumns = 2),
        state = gridState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "curated-actor-profile", span = { GridItemSpan(maxLineSpan) }) {
            CuratedActorProfileHeader(profile = profile)
        }
        val summary = profile.summary
        if (!summary.isNullOrBlank()) {
            item(key = "curated-actor-summary", span = { GridItemSpan(maxLineSpan) }) {
                CuratedActorProfileSection(title = "Summary", body = summary)
            }
        }
        if (profile.externalLinks.isNotEmpty()) {
            item(key = "curated-actor-links", span = { GridItemSpan(maxLineSpan) }) {
                CuratedActorProfileSection(
                    title = "Links",
                    body = profile.externalLinks.joinToString("\n"),
                )
            }
        }
        item(key = "curated-actor-movies-title", span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Movies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        when {
            movies.isLoading -> {
                item(key = "curated-actor-movies-loading", span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            movies.movies.isEmpty() -> {
                item(key = "curated-actor-movies-empty", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "No movies found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                items(movies.movies, key = { it.id }) { movie ->
                    CuratedActorMovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
        if (movies.isLoadingMore || movies.appendErrorMessage != null) {
            item(key = "curated-actor-movies-load-more", span = { GridItemSpan(maxLineSpan) }) {
                CuratedActorMoviesLoadMoreFooter(
                    isLoading = movies.isLoadingMore,
                    errorMessage = movies.appendErrorMessage,
                    onRetryClick = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun CuratedActorProfileHeader(profile: ActorProfile) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CuratedActorAvatar(
            imageUrl = profile.avatarUrl,
            contentDescription = profile.name,
            modifier = Modifier.size(112.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = curatedActorProfileMetadata(profile)
            if (meta.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val tags = profile.userTags.joinToString(", ")
            if (tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tags,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CuratedActorProfileSection(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CuratedActorMovieCard(movie: MovieListItem, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick) {
        val imageUrl = curatedMovieCardImageUrl(movie)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
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
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = curatedActorMovieMetadata(movie),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CuratedActorMoviesLoadMoreFooter(
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
private fun CuratedActorDetailErrorState(message: String, onRetryClick: () -> Unit) {
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

private fun curatedActorProfileMetadata(profile: ActorProfile): String =
    listOf(
            profile.birthday,
            profile.height?.let { "${it}cm" },
            profile.provider,
            profile.homepage,
        )
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" / ")

private fun curatedActorMovieMetadata(movie: MovieListItem): String =
    listOf(movie.code, movie.studio, movie.year.takeIf { it > 0 }?.toString(), movie.resolution)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" / ")
