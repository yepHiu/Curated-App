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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.curated.app.core.R as CoreR
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.api.PlaybackProgress
import dev.curated.app.presentation.utils.rememberSafePadding

@Composable
fun CuratedHistoryScreen(
    onOpenNavigation: (() -> Unit)? = null,
    onPlayMovie: (String, String) -> Unit,
    viewModel: CuratedHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CuratedHistoryLayout(
        state = state,
        onOpenNavigation = onOpenNavigation,
        onPlayMovie = onPlayMovie,
        onRetryClick = viewModel::loadHistory,
    )
}

@Composable
private fun CuratedHistoryLayout(
    state: CuratedHistoryState,
    onOpenNavigation: (() -> Unit)?,
    onPlayMovie: (String, String) -> Unit,
    onRetryClick: () -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = 16.dp, top = safePadding.top + 8.dp, end = 16.dp, bottom = 8.dp)
        ) {
            onOpenNavigation?.let { CuratedNavigationMenuButton(onClick = it) }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                CuratedHistoryErrorState(message = state.errorMessage, onRetryClick = onRetryClick)
            }
            state.items.isEmpty() -> {
                CuratedHistoryEmptyState()
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.movie.id }) { item ->
                        CuratedHistoryCard(
                            item = item,
                            onClick = { onPlayMovie(item.movie.id, item.movie.title) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedHistoryCard(item: CuratedHistoryItem, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        ) {
            CuratedHistoryPoster(movie = item.movie)
            CuratedHistoryCardText(movie = item.movie, progress = item.progress)
        }
    }
}

@Composable
private fun CuratedHistoryPoster(movie: MovieDetail) {
    val imageUrl = curatedHistoryImageUrl(thumbUrl = movie.thumbUrl, coverUrl = movie.coverUrl)
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(88.dp).aspectRatio(0.68f).clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.width(88.dp)
                    .aspectRatio(0.68f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_film),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun CuratedHistoryCardText(movie: MovieDetail, progress: PlaybackProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = curatedHistoryMetadata(movie),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = curatedHistoryProgressTimeText(progress.positionSec, progress.durationSec),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text =
                    curatedHistoryProgressPercentText(
                        progress.positionSec,
                        progress.durationSec,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        CuratedHistoryProgressIndicator(
            progress = {
                curatedHistoryProgressFraction(progress.positionSec, progress.durationSec)
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = progress.updatedAt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuratedHistoryProgressIndicator(progress: () -> Float) {
    LinearProgressIndicator(
        progress = progress,
        gapSize = 0.dp,
        drawStopIndicator = {},
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun curatedHistoryMetadata(movie: MovieDetail): String =
    listOf(movie.code, movie.studio, movie.year.takeIf { it > 0 }?.toString(), movie.resolution)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" / ")

internal fun curatedHistoryHeaderSubtitleResId(itemCount: Int): Int =
    CoreR.string.history_recent_activity

@Composable
private fun CuratedHistoryEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(CoreR.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(CoreR.string.history_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CuratedHistoryErrorState(message: String, onRetryClick: () -> Unit) {
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
