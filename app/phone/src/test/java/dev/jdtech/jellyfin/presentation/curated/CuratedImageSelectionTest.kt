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

    private fun movieDetail(coverUrl: String?, thumbUrl: String?): MovieDetail =
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
            previewImages = emptyList(),
            previewVideoUrl = null,
            metadataRating = 0.0,
            userRating = null,
            actorAvatarUrls = emptyMap(),
        )
}
