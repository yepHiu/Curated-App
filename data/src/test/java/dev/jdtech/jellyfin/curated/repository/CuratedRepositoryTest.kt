package dev.jdtech.jellyfin.curated.repository

import dev.jdtech.jellyfin.curated.api.PlaybackMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CuratedRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: CuratedRepository
    private lateinit var origin: String

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        origin = server.url("/").toString().trimEnd('/')
        val baseUrl = server.url("/api/").toString()
        repository =
            CuratedRepositoryImpl(
                baseUrl = baseUrl,
                client = OkHttpClient(),
                dispatcher = Dispatchers.Unconfined,
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getMoviesReturnsDomainPageWithAbsoluteImageUrls() = runBlocking {
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
                          "coverUrl": "/api/library/movies/movie-1/asset/cover",
                          "thumbUrl": "/api/library/movies/movie-1/asset/thumb"
                        }
                      ],
                      "total": 1,
                      "limit": 20,
                      "offset": 0
                    }
                    """
                        .trimIndent()
                )
        )

        val page = repository.getMovies(limit = 20, offset = 0)
        val request = server.takeRequest()

        assertEquals("/api/library/movies", request.requestUrl?.encodedPath)
        assertEquals(1, page.total)
        assertEquals(20, page.limit)
        assertEquals("$origin/api/library/movies/movie-1/asset/cover", page.items.first().coverUrl)
        assertEquals("$origin/api/library/movies/movie-1/asset/thumb", page.items.first().thumbUrl)
    }

    @Test
    fun getMovieReturnsDomainDetailWithAbsolutePreviewUrls() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": "movie-1",
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
                      "actorAvatarUrls": {"Actor A": "/api/library/actors/Actor%20A/asset/avatar"}
                    }
                    """
                        .trimIndent()
                )
        )

        val movie = repository.getMovie(movieId = "movie-1")

        assertEquals("Example Movie", movie.title)
        assertEquals("$origin/api/library/movies/movie-1/asset/preview/0", movie.previewImages.first())
        assertEquals("$origin/api/library/movies/movie-1/asset/preview-video", movie.previewVideoUrl)
        assertEquals("$origin/api/library/actors/Actor%20A/asset/avatar", movie.actorAvatarUrls["Actor A"])
    }

    @Test
    fun getPlaybackDescriptorReturnsDomainDescriptorWithAbsolutePlaybackUrl() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "movieId": "movie-1",
                      "mode": "hls",
                      "sessionId": "session-1",
                      "sessionKind": "hls",
                      "url": "/api/playback/sessions/session-1/hls/master.m3u8",
                      "durationSec": 3600.5,
                      "resumePositionSec": 42.5,
                      "canDirectPlay": false
                    }
                    """
                        .trimIndent()
                )
        )

        val descriptor = repository.getPlaybackDescriptor(movieId = "movie-1")

        assertEquals(PlaybackMode.Hls, descriptor.mode)
        assertEquals("session-1", descriptor.sessionId)
        assertEquals("$origin/api/playback/sessions/session-1/hls/master.m3u8", descriptor.url)
        assertEquals(42.5, descriptor.resumePositionSec!!, 0.0)
    }

    @Test
    fun getPlaybackProgressReturnsDomainProgressItems() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "items": [
                        {
                          "movieId": "movie-1",
                          "positionSec": 120.5,
                          "durationSec": 7200,
                          "updatedAt": "2026-06-07T12:00:00Z"
                        }
                      ]
                    }
                    """
                        .trimIndent()
                )
        )

        val progress = repository.getPlaybackProgress()
        val request = server.takeRequest()

        assertEquals("/api/playback/progress", request.requestUrl?.encodedPath)
        assertEquals(1, progress.size)
        assertEquals("movie-1", progress.first().movieId)
        assertEquals(120.5, progress.first().positionSec, 0.0)
        assertEquals(7200.0, progress.first().durationSec ?: -1.0, 0.0)
        assertEquals("2026-06-07T12:00:00Z", progress.first().updatedAt)
    }

    @Test
    fun updatePlaybackProgressPutsProgressBodyToBackend() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        repository.updatePlaybackProgress(
            movieId = "movie/1",
            positionSec = 98.25,
            durationSec = 5400.0,
        )
        val request = server.takeRequest()

        assertEquals("/api/playback/progress/movie%2F1", request.requestUrl?.encodedPath)
        assertEquals("PUT", request.method)
        assertEquals("{\"positionSec\":98.25,\"durationSec\":5400.0}", request.body.readUtf8())
    }
}
