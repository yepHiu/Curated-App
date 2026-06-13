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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.presentation.utils.rememberSafePadding

@Composable
fun CuratedHomeScreen(
    onOpenNavigation: (() -> Unit)? = null,
    onMovieClick: (String) -> Unit,
    onPlayMovie: (String, String) -> Unit,
    onOpenMediaClick: () -> Unit,
    viewModel: CuratedHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CuratedHomeLayout(
        state = state,
        onMovieClick = onMovieClick,
        onPlayMovie = onPlayMovie,
        onOpenMediaClick = onOpenMediaClick,
        onOpenNavigation = onOpenNavigation,
        onRetryClick = viewModel::loadHome,
    )
}

@Composable
private fun CuratedHomeLayout(
    state: CuratedHomeState,
    onMovieClick: (String) -> Unit,
    onPlayMovie: (String, String) -> Unit,
    onOpenMediaClick: () -> Unit,
    onOpenNavigation: (() -> Unit)?,
    onRetryClick: () -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Column(modifier = Modifier.fillMaxSize()) {
        CuratedHomeHeader(
            onOpenNavigation = onOpenNavigation,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = curatedHomeHeaderTopPadding(safePadding.top),
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
                CuratedHomeErrorState(message = state.errorMessage, onRetryClick = onRetryClick)
            }
            state.isEmpty -> {
                CuratedHomeEmptyState(onOpenMediaClick = onOpenMediaClick)
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.heroMovies.isNotEmpty()) {
                        item(key = "curated-home-hero") {
                            CuratedHomeHeroSection(
                                movies = state.heroMovies,
                                onMovieClick = onMovieClick,
                                onPlayMovie = onPlayMovie,
                            )
                        }
                    }
                    if (state.todayRecommendations.isNotEmpty()) {
                        item(key = "curated-home-recommendations") {
                            CuratedHomeRecommendationsSection(
                                movies = state.todayRecommendations,
                                onMovieClick = onMovieClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedHomeHeader(
    onOpenNavigation: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        onOpenNavigation?.let { CuratedNavigationMenuButton(onClick = it) }
    }
}

@Composable
private fun CuratedHomeHeroSection(
    movies: List<MovieDetail>,
    onMovieClick: (String) -> Unit,
    onPlayMovie: (String, String) -> Unit,
) {
    Column {
        CuratedHomeSectionTitle(text = "Featured today")
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                CuratedHomeHeroCard(
                    movie = movie,
                    onDetailsClick = { onMovieClick(movie.id) },
                    onPlayClick = { onPlayMovie(movie.id, movie.title) },
                    modifier = Modifier.width(320.dp),
                )
            }
        }
    }
}

@Composable
private fun CuratedHomeHeroCard(
    movie: MovieDetail,
    onDetailsClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(onClick = onDetailsClick, modifier = modifier) {
        CuratedHomeImage(
            imageUrl = curatedHomeHeroImageUrl(movie),
            contentDescription = movie.title,
            modifier = Modifier.fillMaxWidth().height(188.dp),
        )
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = curatedHomeMovieMetadata(movie),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val actors = movie.actors.take(3).joinToString(", ")
            if (actors.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = actors,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPlayClick) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(CoreR.string.play))
                }
                OutlinedButton(onClick = onDetailsClick) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Details")
                }
            }
        }
    }
}

@Composable
private fun CuratedHomeRecommendationsSection(
    movies: List<MovieDetail>,
    onMovieClick: (String) -> Unit,
) {
    Column {
        CuratedHomeSectionTitle(text = "Today's recommendations")
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                CuratedHomeRecommendationCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.width(148.dp),
                )
            }
        }
    }
}

@Composable
private fun CuratedHomeRecommendationCard(
    movie: MovieDetail,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        CuratedHomeImage(
            imageUrl = curatedHomeRecommendationImageUrl(movie),
            contentDescription = movie.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.68f),
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = curatedHomeMovieMetadata(movie),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CuratedHomeSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun CuratedHomeImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
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
                painter = painterResource(CoreR.drawable.ic_film),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun CuratedHomeEmptyState(onOpenMediaClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_home),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "No recommendations yet", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Browse your media library while today's picks are prepared.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenMediaClick) { Text(text = "Browse media") }
    }
}

@Composable
private fun CuratedHomeErrorState(message: String, onRetryClick: () -> Unit) {
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

internal fun curatedHomeHeaderTopPadding(safeDrawingTop: Dp): Dp = safeDrawingTop + 8.dp

internal fun curatedHomeHeroImageUrl(movie: MovieDetail): String? = movie.coverUrl ?: movie.thumbUrl

internal fun curatedHomeRecommendationImageUrl(movie: MovieDetail): String? =
    movie.thumbUrl ?: movie.coverUrl

private fun curatedHomeMovieMetadata(movie: MovieDetail): String =
    listOf(movie.code, movie.studio, movie.year.takeIf { it > 0 }?.toString(), movie.resolution)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" / ")
