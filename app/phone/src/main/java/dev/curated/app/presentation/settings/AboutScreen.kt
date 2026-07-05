package dev.curated.app.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dev.curated.app.BuildConfig
import dev.curated.app.R
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AboutScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val safePaddingStart =
        with(density) { WindowInsets.safeDrawing.getLeft(this, layoutDirection).toDp() }
    val safePaddingEnd =
        with(density) { WindowInsets.safeDrawing.getRight(this, layoutDirection).toDp() }

    val paddingStart = safePaddingStart
    val paddingEnd = safePaddingEnd

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val libraries by produceLibraries(R.raw.aboutlibraries)
    var openSourceExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_arrow_left),
                            contentDescription = null,
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            libraries = if (openSourceExpanded) libraries else null,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = paddingStart + innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = paddingEnd + innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            header = {
                item {
                    AboutHeader(
                        openSourceExpanded = openSourceExpanded,
                        openSourceLibraryCount = libraries?.libraries?.size,
                        onOpenSourceToggle = { openSourceExpanded = !openSourceExpanded },
                        onGithubClick = {
                            try {
                                uriHandler.openUri("https://github.com/wujiahui/curated-droid")
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        onCoffeeClick = {
                            try {
                                uriHandler.openUri("https://ko-fi.com/jarnedemeulemeester")
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun AboutHeader(
    openSourceExpanded: Boolean,
    openSourceLibraryCount: Int?,
    onOpenSourceToggle: () -> Unit,
    onGithubClick: () -> Unit,
    onCoffeeClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacings.default),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(MaterialTheme.spacings.small))
            Image(
                painter = painterResource(CoreR.drawable.ic_banner),
                contentDescription = null,
                modifier = Modifier.width(240.dp),
            )
            Spacer(Modifier.height(MaterialTheme.spacings.medium))
            Text(
                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(MaterialTheme.spacings.small))
            Text(
                text = stringResource(CoreR.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(MaterialTheme.spacings.medium))
            HorizontalDivider()
            Spacer(Modifier.height(MaterialTheme.spacings.medium))
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.small)) {
                FilledTonalIconButton(onClick = onGithubClick) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_github),
                        contentDescription = null,
                    )
                }
                FilledTonalIconButton(onClick = onCoffeeClick) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_coffee),
                        contentDescription = null,
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacings.medium))
            OpenSourceComponentsToggle(
                expanded = openSourceExpanded,
                libraryCount = openSourceLibraryCount,
                onClick = onOpenSourceToggle,
            )
            Spacer(Modifier.height(MaterialTheme.spacings.small))
        }
    }
}

@Composable
private fun OpenSourceComponentsToggle(expanded: Boolean, libraryCount: Int?, onClick: () -> Unit) {
    val actionDescription =
        stringResource(
            if (expanded) {
                CoreR.string.about_open_source_components_collapse
            } else {
                CoreR.string.about_open_source_components_expand
            }
        )
    val summary =
        when {
            expanded -> stringResource(CoreR.string.about_open_source_components_expanded_summary)
            libraryCount != null ->
                stringResource(CoreR.string.about_open_source_components_count, libraryCount)
            else -> stringResource(CoreR.string.about_open_source_components_summary)
        }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacings.medium,
                        vertical = MaterialTheme.spacings.small,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(CoreR.string.about_open_source_components),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painter =
                    painterResource(
                        if (expanded) {
                            CoreR.drawable.ic_chevron_up
                        } else {
                            CoreR.drawable.ic_chevron_down
                        }
                    ),
                contentDescription = actionDescription,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@PreviewScreenSizes
private fun AboutScreenPreview() {
    CuratedTheme { AboutScreen(navigateBack = {}) }
}
