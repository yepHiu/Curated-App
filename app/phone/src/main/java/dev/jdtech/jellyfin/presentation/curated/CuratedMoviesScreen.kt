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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.presentation.utils.GridCellsAdaptiveWithMinColumns

@Composable
fun CuratedMoviesScreen(
    onMovieClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onManageServersClick: () -> Unit,
    viewModel: CuratedMoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CuratedMoviesLayout(
        state = state,
        onMovieClick = onMovieClick,
        onRetryClick = viewModel::loadMovies,
        onSettingsClick = onSettingsClick,
        onManageServersClick = onManageServersClick,
    )
}

@Composable
private fun CuratedMoviesLayout(
    state: CuratedMoviesState,
    onMovieClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManageServersClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Curated",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (state.total > 0) "${state.total} movies" else "Movie library",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onManageServersClick) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_server),
                    contentDescription = "Servers",
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_settings),
                    contentDescription = "Settings",
                )
            }
        }

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
                    Text(text = "No movies found")
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCellsAdaptiveWithMinColumns(minSize = 150.dp, minColumns = 2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.movies, key = { it.id }) { movie ->
                        CuratedMovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedMovieCard(movie: MovieListItem, onClick: () -> Unit) {
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

internal fun curatedMovieCardImageUrl(movie: MovieListItem): String? =
    movie.thumbUrl ?: movie.coverUrl

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
