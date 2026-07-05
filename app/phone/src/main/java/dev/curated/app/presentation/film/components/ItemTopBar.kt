package dev.curated.app.presentation.film.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings
import dev.curated.app.presentation.utils.rememberSafePadding

@Composable
fun ItemTopBar(
    hasBackButton: Boolean,
    hasHomeButton: Boolean,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit) = {},
) {
    val safePadding = rememberSafePadding()

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    start = safePadding.start + MaterialTheme.spacings.small,
                    top = safePadding.top + MaterialTheme.spacings.small,
                    end = safePadding.end + MaterialTheme.spacings.small,
                )
    ) {
        if (hasBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.alpha(0.7f),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                    ),
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_arrow_left),
                    contentDescription = null,
                )
            }
        }
        if (hasHomeButton) {
            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.alpha(0.7f),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                    ),
            ) {
                Icon(painter = painterResource(CoreR.drawable.ic_home), contentDescription = null)
            }
        }
        content()
    }
}

@Composable
@Preview(showBackground = true)
private fun ItemTopBarPreview() {
    CuratedTheme { ItemTopBar(hasBackButton = true, hasHomeButton = true) }
}
