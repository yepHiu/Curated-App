package dev.curated.app.presentation.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.curated.app.core.presentation.theme.Spacings

@Composable
fun CuratedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = curatedDarkScheme, shapes = shapes) {
        CompositionLocalProvider(
            LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background),
            LocalSpacings provides Spacings,
        ) {
            content()
        }
    }
}
