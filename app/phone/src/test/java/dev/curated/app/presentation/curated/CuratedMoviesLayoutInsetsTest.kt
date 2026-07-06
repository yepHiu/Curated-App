package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun moviesUsesSharedTopHeaderWithBrandAndConnectionStatus() {
        val source =
            projectFile("src/main/java/dev/curated/app/presentation/curated/CuratedMoviesScreen.kt")
                .readText()

        assertTrue(source.contains("CuratedPageHeader("))
        assertTrue(source.contains("CuratedBrandWordmark("))
        assertTrue(source.contains("curatedMoviesPageHeaderStatus(state)"))
    }

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
    }
}
