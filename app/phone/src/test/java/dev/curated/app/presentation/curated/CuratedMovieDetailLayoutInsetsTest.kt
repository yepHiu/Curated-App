package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedMovieDetailLayoutInsetsTest {
    @Test
    fun headerTopPaddingKeepsOriginalSpacingWhenThereIsNoStatusBarInset() {
        assertEquals(8.dp, curatedMovieDetailHeaderTopPadding(0.dp))
    }

    @Test
    fun headerTopPaddingAddsSafeDrawingTopInset() {
        assertEquals(32.dp, curatedMovieDetailHeaderTopPadding(24.dp))
    }
}
