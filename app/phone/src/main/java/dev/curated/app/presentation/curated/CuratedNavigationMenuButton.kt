package dev.curated.app.presentation.curated

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.curated.app.core.R as CoreR

@Composable
internal fun CuratedNavigationMenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_menu),
            contentDescription = curatedNavigationMenuContentDescription(),
        )
    }
}

internal fun curatedNavigationMenuContentDescription(): String = "Open navigation"
