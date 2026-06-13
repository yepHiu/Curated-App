package dev.jdtech.jellyfin.presentation.curated

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.presentation.utils.rememberSafePadding

@Composable
fun CuratedMovieDetailScreen(
    movieId: String,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    onPlayMovie: (movieId: String, title: String) -> Unit,
    onActorClick: (String) -> Unit,
    viewModel: CuratedMovieDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) { viewModel.loadMovie(movieId) }

    CuratedMovieDetailLayout(
        state = state,
        navigateBack = navigateBack,
        navigateHome = navigateHome,
        onPlayMovie = onPlayMovie,
        onActorClick = onActorClick,
    )
}

@Composable
private fun CuratedMovieDetailLayout(
    state: CuratedMovieDetailState,
    navigateBack: () -> Unit,
    navigateHome: () -> Unit,
    onPlayMovie: (movieId: String, title: String) -> Unit,
    onActorClick: (String) -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        top = curatedMovieDetailHeaderTopPadding(safePadding.top),
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
                CuratedMovieDetailContent(
                    movie = state.movie,
                    onPlayMovie = onPlayMovie,
                    onActorClick = onActorClick,
                )
            }
        }
    }
}

@Composable
private fun CuratedMovieDetailContent(
    movie: MovieDetail,
    onPlayMovie: (movieId: String, title: String) -> Unit,
    onActorClick: (String) -> Unit,
) {
    val previewImages = curatedMoviePreviewImages(movie)
    val actors = curatedMovieDetailActors(movie)
    var selectedPreviewIndex by rememberSaveable(movie.id) { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Text(stringResource(CoreR.string.play))
                    }
                }
            }

            if (previewImages.isNotEmpty()) {
                item {
                    CuratedMoviePreviewImagesSection(
                        previewImages = previewImages,
                        onPreviewClick = { selectedPreviewIndex = it },
                    )
                }
            }

            if (movie.summary.isNotBlank()) {
                item {
                    CuratedMovieSummarySection(summary = movie.summary)
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CuratedDetailLine(label = "Studio", value = movie.studio)
                    CuratedActorsDetailLine(actors = actors, onActorClick = onActorClick)
                    CuratedDetailLine(label = "Tags", value = movie.tags.joinToString(", "))
                    CuratedDetailLine(label = "Resolution", value = movie.resolution)
                    CuratedDetailLine(
                        label = "Rating",
                        value = movie.userRating?.toString() ?: movie.rating.takeIf { it > 0 }?.toString().orEmpty(),
                    )
                }
            }
        }

        val openPreviewIndex = selectedPreviewIndex
        if (openPreviewIndex != null && previewImages.isNotEmpty()) {
            CuratedPreviewImageDialog(
                previewImages = previewImages,
                initialIndex = openPreviewIndex,
                onDismiss = { selectedPreviewIndex = null },
            )
        }
    }
}

internal fun curatedMovieDetailHeroImageUrl(movie: MovieDetail): String? =
    movie.coverUrl ?: movie.thumbUrl

internal fun curatedMovieDetailHeaderTopPadding(safeDrawingTop: Dp): Dp =
    safeDrawingTop + 8.dp

internal fun curatedMoviePreviewImages(movie: MovieDetail): List<String> =
    movie.previewImages.filter { it.isNotBlank() }.distinct()

internal fun curatedMovieDetailActors(movie: MovieDetail): List<String> =
    movie.actors.map { it.trim() }.filter { it.isNotBlank() }.distinct()

internal fun curatedPreviewCanGoPrevious(index: Int): Boolean = index > 0

internal fun curatedPreviewCanGoNext(index: Int, total: Int): Boolean =
    total > 0 && index < total - 1

internal fun curatedPreviewPositionText(index: Int, total: Int): String =
    if (total <= 0) "" else "${index + 1} / $total"

internal fun curatedPreviewThumbnailAspectRatio(width: Int, height: Int): Float {
    if (width <= 0 || height <= 0) return CuratedPreviewThumbnailDefaultAspectRatio

    return (width.toFloat() / height.toFloat())
        .coerceIn(
            minimumValue = CuratedPreviewThumbnailMinAspectRatio,
            maximumValue = CuratedPreviewThumbnailMaxAspectRatio,
        )
}

internal fun curatedMovieSummaryCanToggle(
    lineCount: Int,
    hasVisualOverflow: Boolean,
): Boolean = hasVisualOverflow || lineCount > CuratedMovieSummaryCollapsedMaxLines

internal fun curatedMovieSummaryMaxLines(isExpanded: Boolean): Int =
    if (isExpanded) Int.MAX_VALUE else CuratedMovieSummaryCollapsedMaxLines

@Composable
private fun CuratedMovieSummarySection(summary: String) {
    var isExpanded by rememberSaveable(summary) { mutableStateOf(false) }
    var canToggle by remember(summary) { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = curatedMovieSummaryMaxLines(isExpanded),
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    canToggle =
                        curatedMovieSummaryCanToggle(
                            lineCount = textLayoutResult.lineCount,
                            hasVisualOverflow = textLayoutResult.hasVisualOverflow,
                        )
                }
            },
        )
        if (canToggle) {
            TextButton(onClick = { isExpanded = !isExpanded }) {
                Text(
                    text =
                        stringResource(
                            if (isExpanded) {
                                CoreR.string.movie_summary_collapse
                            } else {
                                CoreR.string.movie_summary_expand
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun CuratedMoviePreviewImagesSection(
    previewImages: List<String>,
    onPreviewClick: (Int) -> Unit,
) {
    if (previewImages.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(CoreR.string.movie_preview_images),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            itemsIndexed(previewImages) { index, imageUrl ->
                CuratedMoviePreviewThumbnail(
                    imageUrl = imageUrl,
                    index = index,
                    onClick = { onPreviewClick(index) },
                )
            }
        }
    }
}

@Composable
private fun CuratedMoviePreviewThumbnail(
    imageUrl: String,
    index: Int,
    onClick: () -> Unit,
) {
    val thumbnailHeight = 96.dp
    var thumbnailAspectRatio by
        rememberSaveable(imageUrl) { mutableStateOf(CuratedPreviewThumbnailDefaultAspectRatio) }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier.width(thumbnailHeight * thumbnailAspectRatio)
                .height(thumbnailHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription =
                stringResource(
                    CoreR.string.movie_preview_image_description,
                    index + 1,
                ),
            contentScale = ContentScale.Fit,
            error = painterResource(CoreR.drawable.ic_film),
            onSuccess = { state ->
                thumbnailAspectRatio =
                    curatedPreviewThumbnailAspectRatio(
                        width = state.result.image.width,
                        height = state.result.image.height,
                    )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CuratedPreviewImageDialog(
    previewImages: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    if (previewImages.isEmpty()) return

    val safePadding = rememberSafePadding(handleStartInsets = false)
    var currentIndex by
        rememberSaveable(initialIndex, previewImages.size) {
            mutableStateOf(initialIndex.coerceIn(0, previewImages.lastIndex))
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .padding(
                        start = 16.dp,
                        top = safePadding.top + 8.dp,
                        end = 16.dp,
                        bottom = safePadding.bottom + 16.dp,
                    )
        ) {
            AsyncImage(
                model = previewImages[currentIndex],
                contentDescription = curatedPreviewPositionText(currentIndex, previewImages.size),
                contentScale = ContentScale.Fit,
                error = painterResource(CoreR.drawable.ic_film),
                modifier = Modifier.fillMaxSize().padding(vertical = 56.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
            ) {
                Text(
                    text = curatedPreviewPositionText(currentIndex, previewImages.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_x),
                        contentDescription = stringResource(CoreR.string.movie_preview_close),
                        tint = Color.White,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            ) {
                FilledTonalIconButton(
                    enabled = curatedPreviewCanGoPrevious(currentIndex),
                    onClick = { currentIndex -= 1 },
                ) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_arrow_left),
                        contentDescription = stringResource(CoreR.string.movie_preview_previous),
                    )
                }
                FilledTonalIconButton(
                    enabled = curatedPreviewCanGoNext(currentIndex, previewImages.size),
                    onClick = { currentIndex += 1 },
                ) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_arrow_right),
                        contentDescription = stringResource(CoreR.string.movie_preview_next),
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CuratedActorsDetailLine(
    actors: List<String>,
    onActorClick: (String) -> Unit,
) {
    if (actors.isEmpty()) return

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "Actors",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            actors.forEach { actor ->
                AssistChip(
                    onClick = { onActorClick(actor) },
                    label = {
                        Text(
                            text = actor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

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

private val CuratedPreviewThumbnailDefaultAspectRatio = 16f / 9f
private val CuratedPreviewThumbnailMinAspectRatio = 9f / 16f
private val CuratedPreviewThumbnailMaxAspectRatio = 21f / 9f
private val CuratedMovieSummaryCollapsedMaxLines = 4
