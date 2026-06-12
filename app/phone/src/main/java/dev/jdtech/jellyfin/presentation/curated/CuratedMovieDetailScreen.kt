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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.jdtech.jellyfin.curated.api.MovieDetail

@Composable
fun CuratedMovieDetailScreen(
    movieId: String,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    onPlayMovie: (movieId: String, title: String) -> Unit,
    viewModel: CuratedMovieDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) { viewModel.loadMovie(movieId) }

    CuratedMovieDetailLayout(
        state = state,
        navigateBack = navigateBack,
        navigateHome = navigateHome,
        onPlayMovie = onPlayMovie,
    )
}

@Composable
private fun CuratedMovieDetailLayout(
    state: CuratedMovieDetailState,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    onPlayMovie: (movieId: String, title: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = navigateBack) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_left),
                    contentDescription = "Back",
                )
            }
            Text(
                text = state.movie?.title ?: "Movie",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = navigateHome) {
                Icon(painter = painterResource(CoreR.drawable.ic_home), contentDescription = "Home")
            }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
            state.movie != null -> {
                CuratedMovieDetailContent(movie = state.movie, onPlayMovie = onPlayMovie)
            }
        }
    }
}

@Composable
private fun CuratedMovieDetailContent(
    movie: MovieDetail,
    onPlayMovie: (movieId: String, title: String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            val imageUrl = curatedMovieDetailHeroImageUrl(movie)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_film),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = curatedMovieMeta(movie),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onPlayMovie(movie.id, movie.title) }) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_play),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play")
                }
            }
        }

        if (movie.summary.isNotBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = movie.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                CuratedDetailLine(label = "Studio", value = movie.studio)
                CuratedDetailLine(label = "Actors", value = movie.actors.joinToString(", "))
                CuratedDetailLine(label = "Tags", value = movie.tags.joinToString(", "))
                CuratedDetailLine(label = "Resolution", value = movie.resolution)
                CuratedDetailLine(
                    label = "Rating",
                    value = movie.userRating?.toString() ?: movie.rating.takeIf { it > 0 }?.toString().orEmpty(),
                )
            }
        }
    }
}

internal fun curatedMovieDetailHeroImageUrl(movie: MovieDetail): String? =
    movie.coverUrl ?: movie.thumbUrl

@Composable
private fun CuratedDetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

private fun curatedMovieMeta(movie: MovieDetail): String =
    listOf(
            movie.code,
            movie.year.takeIf { it > 0 }?.toString(),
            movie.runtimeMinutes.takeIf { it > 0 }?.let { "$it min" },
            movie.releaseDate,
        )
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" · ")
