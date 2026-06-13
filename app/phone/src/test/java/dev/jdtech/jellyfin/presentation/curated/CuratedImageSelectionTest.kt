package dev.jdtech.jellyfin.presentation.curated

import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MovieListItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedImageSelectionTest {
    @Test
    fun movieCardPrefersNarrowThumbnail() {
        val movie = movieListItem(coverUrl = "wide-cover", thumbUrl = "narrow-thumb")

        assertEquals("narrow-thumb", curatedMovieCardImageUrl(movie))
    }

    @Test
    fun movieCardFallsBackToCoverWhenThumbnailIsMissing() {
        val movie = movieListItem(coverUrl = "wide-cover", thumbUrl = null)

        assertEquals("wide-cover", curatedMovieCardImageUrl(movie))
    }

    @Test
    fun movieDetailHeroPrefersWideCover() {
        val movie = movieDetail(coverUrl = "wide-cover", thumbUrl = "narrow-thumb")

        assertEquals("wide-cover", curatedMovieDetailHeroImageUrl(movie))
    }

    @Test
    fun movieDetailHeroFallsBackToThumbnailWhenCoverIsMissing() {
        val movie = movieDetail(coverUrl = null, thumbUrl = "narrow-thumb")

        assertEquals("narrow-thumb", curatedMovieDetailHeroImageUrl(movie))
    }

    @Test
    fun movieDetailPreviewImagesDropsBlankUrlsAndDeduplicates() {
        val movie =
            movieDetail(
                coverUrl = "wide-cover",
                thumbUrl = "narrow-thumb",
                previewImages = listOf("", " ", "url-a", "url-a", "url-b"),
            )

        assertEquals(listOf("url-a", "url-b"), curatedMoviePreviewImages(movie))
    }

    @Test
    fun previewNavigationBoundsReflectCurrentIndex() {
        assertEquals(false, curatedPreviewCanGoPrevious(index = 0))
        assertEquals(true, curatedPreviewCanGoNext(index = 0, total = 3))

        assertEquals(true, curatedPreviewCanGoPrevious(index = 1))
        assertEquals(true, curatedPreviewCanGoNext(index = 1, total = 3))

        assertEquals(true, curatedPreviewCanGoPrevious(index = 2))
        assertEquals(false, curatedPreviewCanGoNext(index = 2, total = 3))
    }

    @Test
    fun previewPositionTextUsesOneBasedIndex() {
        assertEquals("1 / 3", curatedPreviewPositionText(index = 0, total = 3))
        assertEquals("3 / 3", curatedPreviewPositionText(index = 2, total = 3))
        assertEquals("", curatedPreviewPositionText(index = 0, total = 0))
    }

    @Test
    fun previewThumbnailAspectRatioUsesImageDimensions() {
        assertEquals(1f, curatedPreviewThumbnailAspectRatio(width = 1200, height = 1200), 0.0001f)
        assertEquals(2f / 3f, curatedPreviewThumbnailAspectRatio(width = 800, height = 1200), 0.0001f)
        assertEquals(21f / 9f, curatedPreviewThumbnailAspectRatio(width = 2100, height = 900), 0.0001f)
    }

    @Test
    fun previewThumbnailAspectRatioFallsBackAndClampsExtremes() {
        assertEquals(16f / 9f, curatedPreviewThumbnailAspectRatio(width = 0, height = 900), 0.0001f)
        assertEquals(16f / 9f, curatedPreviewThumbnailAspectRatio(width = 1600, height = 0), 0.0001f)
        assertEquals(9f / 16f, curatedPreviewThumbnailAspectRatio(width = 100, height = 1000), 0.0001f)
        assertEquals(21f / 9f, curatedPreviewThumbnailAspectRatio(width = 4000, height = 500), 0.0001f)
    }

    @Test
    fun movieSummaryToggleOnlyShowsWhenCollapsedTextOverflows() {
        assertEquals(false, curatedMovieSummaryCanToggle(lineCount = 4, hasVisualOverflow = false))
        assertEquals(true, curatedMovieSummaryCanToggle(lineCount = 5, hasVisualOverflow = false))
        assertEquals(true, curatedMovieSummaryCanToggle(lineCount = 4, hasVisualOverflow = true))
    }

    @Test
    fun movieSummaryMaxLinesReflectExpandedState() {
        assertEquals(4, curatedMovieSummaryMaxLines(isExpanded = false))
        assertEquals(Int.MAX_VALUE, curatedMovieSummaryMaxLines(isExpanded = true))
    }

    private fun movieListItem(coverUrl: String?, thumbUrl: String?): MovieListItem =
        MovieListItem(
            id = "movie-1",
            title = "Example",
            code = "ABC-001",
            studio = "Studio",
            actors = emptyList(),
            tags = emptyList(),
            userTags = emptyList(),
            runtimeMinutes = 120,
            rating = 0.0,
            isFavorite = false,
            addedAt = "",
            location = "",
            resolution = "",
            year = 2026,
            releaseDate = null,
            coverUrl = coverUrl,
            thumbUrl = thumbUrl,
            trashedAt = null,
        )

    private fun movieDetail(
        coverUrl: String?,
        thumbUrl: String?,
        previewImages: List<String> = emptyList(),
    ): MovieDetail =
        MovieDetail(
            id = "movie-1",
            title = "Example",
            code = "ABC-001",
            studio = "Studio",
            actors = emptyList(),
            tags = emptyList(),
            userTags = emptyList(),
            runtimeMinutes = 120,
            rating = 0.0,
            isFavorite = false,
            addedAt = "",
            location = "",
            resolution = "",
            year = 2026,
            releaseDate = null,
            coverUrl = coverUrl,
            thumbUrl = thumbUrl,
            trashedAt = null,
            summary = "",
            previewImages = previewImages,
            previewVideoUrl = null,
            metadataRating = 0.0,
            userRating = null,
            actorAvatarUrls = emptyMap(),
        )
}
