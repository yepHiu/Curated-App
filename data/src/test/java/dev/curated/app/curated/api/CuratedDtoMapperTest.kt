package dev.curated.app.curated.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CuratedDtoMapperTest {
    private val baseUrl = "http://192.168.1.23:8080/api"

    @Test
    fun mapsMovieListItemDtoToDomainWithAbsoluteImageUrls() {
        val dto =
            MovieListItemDto(
                id = "movie-1",
                title = "Example Title",
                code = "ABC-001",
                studio = "Studio",
                actors = listOf("Actor A"),
                tags = listOf("metadata"),
                userTags = listOf("favorite"),
                runtimeMinutes = 120,
                rating = 4.5,
                isFavorite = true,
                addedAt = "2026-06-07T12:00:00Z",
                location = "D:\\Library\\ABC-001.mp4",
                resolution = "1080p",
                year = 2026,
                releaseDate = "2026-01-01",
                coverUrl = "/api/library/movies/movie-1/asset/cover?v=1",
                thumbUrl = "/api/library/movies/movie-1/asset/thumb?v=1",
            )

        val movie = dto.toDomain(baseUrl)

        assertEquals("movie-1", movie.id)
        assertEquals("Example Title", movie.title)
        assertEquals("ABC-001", movie.code)
        assertEquals(listOf("Actor A"), movie.actors)
        assertEquals(listOf("favorite"), movie.userTags)
        assertEquals("http://192.168.1.23:8080/api/library/movies/movie-1/asset/cover?v=1", movie.coverUrl)
        assertEquals("http://192.168.1.23:8080/api/library/movies/movie-1/asset/thumb?v=1", movie.thumbUrl)
    }

    @Test
    fun mapsMovieDetailDtoToDomainWithPreviewImagesAndActorAvatars() {
        val dto =
            MovieDetailDto(
                id = "movie-1",
                title = "Example Title",
                code = "ABC-001",
                studio = "Studio",
                actors = listOf("Actor A"),
                tags = emptyList(),
                userTags = emptyList(),
                runtimeMinutes = 120,
                rating = 4.5,
                isFavorite = false,
                addedAt = "2026-06-07T12:00:00Z",
                location = "D:\\Library\\ABC-001.mp4",
                resolution = "1080p",
                year = 2026,
                releaseDate = null,
                coverUrl = null,
                thumbUrl = null,
                summary = "Summary",
                previewImages = listOf("/api/library/movies/movie-1/asset/preview/1"),
                previewVideoUrl = null,
                metadataRating = 4.0,
                userRating = null,
                actorAvatarUrls = mapOf("Actor A" to "/api/library/actors/Actor%20A/asset/avatar"),
            )

        val movie = dto.toDomain(baseUrl)

        assertEquals("Summary", movie.summary)
        assertEquals(listOf("http://192.168.1.23:8080/api/library/movies/movie-1/asset/preview/1"), movie.previewImages)
        assertEquals("http://192.168.1.23:8080/api/library/actors/Actor%20A/asset/avatar", movie.actorAvatarUrls["Actor A"])
        assertNull(movie.userRating)
    }

    @Test
    fun mapsPlaybackDescriptorDtoToDomainWithAbsoluteUrl() {
        val dto =
            PlaybackDescriptorDto(
                movieId = "movie-1",
                mode = "hls",
                sessionId = "session-1",
                sessionKind = "ffmpeg",
                url = "/api/playback/sessions/session-1/hls/index.m3u8",
                mimeType = "application/vnd.apple.mpegurl",
                fileName = "ABC-001.m3u8",
                durationSec = 7200.0,
                startPositionSec = null,
                resumePositionSec = 120.5,
                canDirectPlay = false,
                reasonCode = "TRANSCODE_REQUIRED",
                reasonMessage = "HLS session required",
            )

        val descriptor = dto.toDomain(baseUrl)

        assertEquals("movie-1", descriptor.movieId)
        assertEquals(PlaybackMode.Hls, descriptor.mode)
        assertEquals("session-1", descriptor.sessionId)
        assertEquals("http://192.168.1.23:8080/api/playback/sessions/session-1/hls/index.m3u8", descriptor.url)
        assertEquals(120.5, descriptor.resumePositionSec ?: -1.0, 0.0)
    }

    @Test
    fun mapsPlaybackProgressDtoToDomain() {
        val dto =
            PlaybackProgressDto(
                movieId = "movie-1",
                positionSec = 120.5,
                durationSec = 7200.0,
                updatedAt = "2026-06-07T12:00:00Z",
            )

        val progress = dto.toDomain()

        assertEquals("movie-1", progress.movieId)
        assertEquals(120.5, progress.positionSec, 0.0)
        assertEquals(7200.0, progress.durationSec ?: -1.0, 0.0)
        assertEquals("2026-06-07T12:00:00Z", progress.updatedAt)
    }
}
