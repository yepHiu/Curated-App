package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import dev.curated.app.curated.api.MovieDetail
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedHomeLayoutTest {
    @Test
    fun headerTopPaddingIncludesSafeDrawingTop() {
        assertEquals(8.dp, curatedHomeHeaderTopPadding(0.dp))
        assertEquals(32.dp, curatedHomeHeaderTopPadding(24.dp))
    }

    @Test
    fun heroImagePrefersCoverAndFallsBackToThumbnail() {
        assertEquals(
            "cover",
            curatedHomeHeroImageUrl(movieDetail(coverUrl = "cover", thumbUrl = "thumb")),
        )
        assertEquals(
            "thumb",
            curatedHomeHeroImageUrl(movieDetail(coverUrl = null, thumbUrl = "thumb")),
        )
    }

    @Test
    fun recommendationImagePrefersThumbnailAndFallsBackToCover() {
        assertEquals(
            "thumb",
            curatedHomeRecommendationImageUrl(movieDetail(coverUrl = "cover", thumbUrl = "thumb")),
        )
        assertEquals(
            "cover",
            curatedHomeRecommendationImageUrl(movieDetail(coverUrl = "cover", thumbUrl = null)),
        )
    }

    @Test
    fun homeCardTitlesStaySingleLine() {
        val source =
            projectFile("src/main/java/dev/curated/app/presentation/curated/CuratedHomeScreen.kt")
                .readText()

        listOf("CuratedHomeHeroCard", "CuratedHomeRecommendationCard").forEach { functionName ->
            val titleBlock = source.titleTextBlock(functionName)

            assertTrue("$functionName title should be single line", titleBlock.contains("maxLines = 1"))
            assertFalse("$functionName title should not wrap to two lines", titleBlock.contains("maxLines = 2"))
        }
    }

    @Test
    fun homeUsesSharedTopHeaderWithBrandAndConnectionStatus() {
        val source =
            projectFile("src/main/java/dev/curated/app/presentation/curated/CuratedHomeScreen.kt")
                .readText()

        assertTrue(source.contains("CuratedPageHeader("))
        assertTrue(source.contains("CuratedBrandWordmark("))
        assertTrue(source.contains("curatedHomePageHeaderStatus(state)"))
    }

    private fun movieDetail(coverUrl: String?, thumbUrl: String?): MovieDetail =
        MovieDetail(
            id = "movie-1",
            title = "Movie",
            code = "ABC-001",
            studio = "Studio",
            actors = emptyList(),
            tags = emptyList(),
            userTags = emptyList(),
            runtimeMinutes = 120,
            rating = 0.0,
            isFavorite = false,
            addedAt = "2026-06-13T12:00:00Z",
            location = "",
            resolution = "1080p",
            year = 2026,
            releaseDate = null,
            coverUrl = coverUrl,
            thumbUrl = thumbUrl,
            trashedAt = null,
            summary = "",
            previewImages = emptyList(),
            previewVideoUrl = null,
            metadataRating = 0.0,
            userRating = null,
            actorAvatarUrls = emptyMap(),
        )

    private fun String.titleTextBlock(functionName: String): String =
        substringAfter("private fun $functionName")
            .substringAfter("text = movie.title,")
            .substringBefore(")")

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
    }
}
