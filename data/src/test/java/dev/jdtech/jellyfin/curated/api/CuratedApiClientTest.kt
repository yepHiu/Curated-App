package dev.jdtech.jellyfin.curated.api

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class CuratedApiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var api: CuratedApiClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        api =
            CuratedApiClient(
                baseUrl = server.url("/api/").toString(),
                client = OkHttpClient(),
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getHealthRequestsHealthEndpointAndParsesResponse() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "name": "curated-dev",
                      "version": "git.abcdef0",
                      "channel": "dev",
                      "installerVersion": "0.0.0",
                      "transport": "http",
                      "databasePath": "C:\\Users\\curated.db"
                    }
                    """
                        .trimIndent()
                )
        )

        val health = api.getHealth()
        val request = server.takeRequest()

        assertEquals("/api/health", request.path)
        assertEquals("curated-dev", health.name)
        assertEquals("git.abcdef0", health.version)
        assertEquals("dev", health.channel)
    }

    @Test
    fun getAuthStatusRequestsAuthStatusEndpointAndParsesResponse() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "pinEnabled": true,
                      "unlocked": false,
                      "setupRequired": false,
                      "pinLength": 4,
                      "trustedForever": false,
                      "sessionTtlMinutes": 60,
                      "lanRequiresPin": true,
                      "lockOnRestart": true
                    }
                    """
                        .trimIndent()
                )
        )

        val status = api.getAuthStatus()
        val request = server.takeRequest()

        assertEquals("/api/auth/status", request.path)
        assertEquals(true, status.pinEnabled)
        assertEquals(false, status.unlocked)
        assertEquals(4, status.pinLength)
    }

    @Test
    fun unlockPostsPinAndTrustedForeverFlag() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "pinEnabled": true,
                      "unlocked": true,
                      "setupRequired": false,
                      "pinLength": 4,
                      "trustedForever": true,
                      "sessionTtlMinutes": 60,
                      "lanRequiresPin": true,
                      "lockOnRestart": true
                    }
                    """
                        .trimIndent()
                )
        )

        val status = api.unlock(pin = "1234", trustedForever = true)
        val request = server.takeRequest()

        assertEquals("/api/auth/unlock", request.path)
        assertEquals("POST", request.method)
        assertEquals("""{"pin":"1234","trustedForever":true}""", request.body.readUtf8())
        assertEquals(true, status.unlocked)
        assertEquals(true, status.trustedForever)
    }

    @Test
    fun protectedRequestThrowsMappedFailureWhenBackendReturnsAppError() {
        server.enqueue(
            MockResponse()
                .setResponseCode(423)
                .setBody("""{"code":"AUTH_LOCKED","message":"locked","retryable":false}""")
        )

        val exception = assertThrows(CuratedApiException::class.java) { api.getAuthStatus() }

        assertEquals(CuratedFailure.AuthLocked, exception.failure.kind)
        assertEquals("AUTH_LOCKED", exception.failure.code)
    }

    @Test
    fun getMoviesRequestsMovieListEndpointWithSupportedFilters() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "items": [
                        {
                          "id": "movie-1",
                          "title": "Example Movie",
                          "code": "ABC-001",
                          "studio": "Studio A",
                          "actors": ["Actor A"],
                          "tags": ["tag-a"],
                          "userTags": ["favorite"],
                          "runtimeMinutes": 120,
                          "rating": 4.5,
                          "isFavorite": true,
                          "addedAt": "2026-06-07T12:00:00Z",
                          "location": "D:\\Movies\\ABC-001.mp4",
                          "resolution": "1080p",
                          "year": 2026,
                          "releaseDate": "2026-06-01",
                          "coverUrl": "/api/library/movies/movie-1/asset/cover",
                          "thumbUrl": "/api/library/movies/movie-1/asset/thumb"
                        }
                      ],
                      "total": 1,
                      "limit": 24,
                      "offset": 48
                    }
                    """
                        .trimIndent()
                )
        )

        val page =
            api.getMovies(
                limit = 24,
                offset = 48,
                query = "Example Movie",
                actor = "Actor A",
                studio = "Studio A",
                mode = "active",
            )
        val request = server.takeRequest()

        assertEquals("/api/library/movies", request.requestUrl?.encodedPath)
        assertEquals("24", request.requestUrl?.queryParameter("limit"))
        assertEquals("48", request.requestUrl?.queryParameter("offset"))
        assertEquals("Example Movie", request.requestUrl?.queryParameter("q"))
        assertEquals("Actor A", request.requestUrl?.queryParameter("actor"))
        assertEquals("Studio A", request.requestUrl?.queryParameter("studio"))
        assertEquals("active", request.requestUrl?.queryParameter("mode"))
        assertEquals(1, page.items.size)
        assertEquals("movie-1", page.items.first().id)
        assertEquals(1, page.total)
        assertEquals(24, page.limit)
        assertEquals(48, page.offset)
    }

    @Test
    fun getMovieEncodesMovieIdPathSegmentAndParsesDetail() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": "movie/1",
                      "title": "Example Movie",
                      "code": "ABC-001",
                      "studio": "Studio A",
                      "actors": ["Actor A"],
                      "tags": ["tag-a"],
                      "userTags": [],
                      "runtimeMinutes": 120,
                      "rating": 4.5,
                      "isFavorite": false,
                      "addedAt": "2026-06-07T12:00:00Z",
                      "location": "D:\\Movies\\ABC-001.mp4",
                      "resolution": "1080p",
                      "year": 2026,
                      "coverUrl": "/api/library/movies/movie-1/asset/cover",
                      "thumbUrl": "/api/library/movies/movie-1/asset/thumb",
                      "summary": "A movie summary.",
                      "previewImages": ["/api/library/movies/movie-1/asset/preview/0"],
                      "previewVideoUrl": "/api/library/movies/movie-1/asset/preview-video",
                      "metadataRating": 4.8,
                      "userRating": 4.0,
                      "actorAvatarUrls": {"Actor A": "/api/library/actors/Actor%20A/asset/avatar"}
                    }
                    """
                        .trimIndent()
                )
        )

        val movie = api.getMovie(movieId = "movie/1")
        val request = server.takeRequest()

        assertEquals("/api/library/movies/movie%2F1", request.requestUrl?.encodedPath)
        assertEquals("movie/1", movie.id)
        assertEquals("A movie summary.", movie.summary)
        assertEquals(4.8, movie.metadataRating, 0.0)
    }

    @Test
    fun getPlaybackDescriptorEncodesMovieIdAndParsesDescriptor() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "movieId": "movie/1",
                      "mode": "hls",
                      "sessionId": "session-1",
                      "sessionKind": "hls",
                      "url": "/api/playback/sessions/session-1/hls/master.m3u8",
                      "mimeType": "application/vnd.apple.mpegurl",
                      "fileName": "ABC-001.mp4",
                      "transcodeProfile": "default",
                      "durationSec": 3600.5,
                      "startPositionSec": 0,
                      "resumePositionSec": 42.5,
                      "canDirectPlay": false,
                      "reason": "transcode",
                      "reasonCode": "HLS_REQUIRED",
                      "reasonMessage": "HLS playback required",
                      "sourceContainer": "mp4",
                      "sourceVideoCodec": "h264",
                      "sourceAudioCodec": "aac"
                    }
                    """
                        .trimIndent()
                )
        )

        val descriptor = api.getPlaybackDescriptor(movieId = "movie/1")
        val request = server.takeRequest()

        assertEquals("/api/library/movies/movie%2F1/playback", request.requestUrl?.encodedPath)
        assertEquals("movie/1", descriptor.movieId)
        assertEquals("hls", descriptor.mode)
        assertEquals("session-1", descriptor.sessionId)
        assertEquals(42.5, descriptor.resumePositionSec!!, 0.0)
    }
}
