package dev.curated.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.compositionLocalOf
import dev.curated.app.core.presentation.theme.Spacings

val MaterialTheme.spacings
    get() = Spacings

val LocalSpacings = compositionLocalOf { MaterialTheme.spacings }
