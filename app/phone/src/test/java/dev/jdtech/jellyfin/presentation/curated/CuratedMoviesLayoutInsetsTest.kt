package dev.jdtech.jellyfin.presentation.curated

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedMoviesLayoutInsetsTest {
    @Test
    fun headerTopPaddingKeepsOriginalSpacingWhenThereIsNoStatusBarInset() {
        assertEquals(8.dp, curatedMoviesHeaderTopPadding(0.dp))
    }

    @Test
    fun headerTopPaddingAddsSafeDrawingTopInset() {
        assertEquals(32.dp, curatedMoviesHeaderTopPadding(24.dp))
    }

    @Test
    fun headerSubtitleDoesNotShowMovieCount() {
        assertEquals("Movie library", curatedMoviesHeaderSubtitle(total = 125))
    }

    @Test
    fun headerActionsExposeSearchEntry() {
        assertEquals(listOf("Search"), curatedMoviesHeaderActionContentDescriptions())
    }
}
