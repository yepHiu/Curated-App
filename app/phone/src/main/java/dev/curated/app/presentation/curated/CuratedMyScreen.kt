package dev.curated.app.presentation.curated

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.utils.rememberSafePadding

@Composable
fun CuratedMyScreen(
    onOpenNavigation: (() -> Unit)? = null,
    onActorsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    bottomContentPadding: Dp = 16.dp,
) {
    val safePadding = rememberSafePadding(handleStartInsets = false)

    Column(modifier = Modifier.fillMaxSize()) {
        CuratedMyHeader(
            onOpenNavigation = onOpenNavigation,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = safePadding.top + 8.dp,
                        end = 16.dp,
                        bottom = 8.dp,
                    ),
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = bottomContentPadding,
                ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "curated-my-actors") {
                CuratedMyEntryCard(
                    title = CoreR.string.title_actors,
                    icon = CoreR.drawable.ic_user,
                    onClick = onActorsClick,
                )
            }
            item(key = "curated-my-history") {
                CuratedMyEntryCard(
                    title = CoreR.string.title_history,
                    icon = CoreR.drawable.ic_history,
                    onClick = onHistoryClick,
                )
            }
        }
    }
}

@Composable
private fun CuratedMyHeader(onOpenNavigation: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        onOpenNavigation?.let {
            CuratedNavigationMenuButton(onClick = it)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = stringResource(CoreR.string.title_my),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CuratedMyEntryCard(
    @StringRes title: Int,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(CoreR.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
