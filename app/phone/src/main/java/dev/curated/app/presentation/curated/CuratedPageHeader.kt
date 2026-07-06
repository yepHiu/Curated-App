package dev.curated.app.presentation.curated

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.utils.rememberSafePadding

private val CuratedPageHeaderHorizontalPadding = 16.dp
private val CuratedPageHeaderTopSpacing = 8.dp
private val CuratedPageHeaderContentHeight = 56.dp
private val CuratedPageHeaderBottomSpacing = 8.dp

internal enum class CuratedPageHeaderStatus {
    Connecting,
    Reconnecting,
}

@Composable
internal fun CuratedPageHeader(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = CuratedPageHeaderHorizontalPadding,
                    top = curatedPageHeaderTopPadding(safePadding.top),
                    end = CuratedPageHeaderHorizontalPadding,
                    bottom = curatedPageHeaderBottomPadding(),
                )
                .height(curatedPageHeaderContentHeight()),
        content = content,
    )
}

@Composable
internal fun CuratedBrandWordmark(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_sparkles),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = curatedPageHeaderBrandWordmarkText(),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun CuratedPageHeaderTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
internal fun CuratedPageHeaderStatusChip(
    status: CuratedPageHeaderStatus,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colorScheme.surfaceContainerHighest.copy(alpha = 0.68f),
        contentColor = colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = 32.dp).padding(horizontal = 10.dp),
        ) {
            Icon(
                painter =
                    painterResource(
                        when (status) {
                            CuratedPageHeaderStatus.Connecting -> CoreR.drawable.ic_server
                            CuratedPageHeaderStatus.Reconnecting -> CoreR.drawable.ic_server_off
                        }
                    ),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = curatedPageHeaderStatusLabel(status),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun CuratedConnectionErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ColumnCentered(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_server_off),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Library is out of reach",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message.ifBlank { "The connection is taking a moment." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryClick) { Text(text = stringResource(CoreR.string.retry)) }
    }
}

@Composable
private fun ColumnCentered(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
        content = content,
    )
}

internal fun curatedPageHeaderTopPadding(safeDrawingTop: Dp): Dp =
    safeDrawingTop + CuratedPageHeaderTopSpacing

internal fun curatedPageHeaderContentHeight(): Dp = CuratedPageHeaderContentHeight

internal fun curatedPageHeaderBottomPadding(): Dp = CuratedPageHeaderBottomSpacing

internal fun curatedPageHeaderBrandWordmarkText(): String = "creative"

internal fun curatedPageHeaderStatusLabel(status: CuratedPageHeaderStatus): String =
    when (status) {
        CuratedPageHeaderStatus.Connecting -> "Connecting"
        CuratedPageHeaderStatus.Reconnecting -> "Reconnecting"
    }
