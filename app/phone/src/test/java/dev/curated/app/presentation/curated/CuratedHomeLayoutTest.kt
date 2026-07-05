package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import dev.curated.app.curated.api.MovieDetail
import org.junit.Assert.assertEquals
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
}
