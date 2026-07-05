package dev.curated.app

import dev.curated.app.curated.api.PlaybackDescriptor
import dev.curated.app.curated.api.PlaybackMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedPlayerContractTest {
    @Test
    fun curatedPlayerExtrasKeepStringMovieId() {
        val extras = curatedPlayerExtras(movieId = "movie/ABC-001", title = "Example")

        assertEquals("movie/ABC-001", extras[CuratedPlayerContract.EXTRA_MOVIE_ID])
        assertEquals("Example", extras[CuratedPlayerContract.EXTRA_TITLE])
    }

    @Test
    fun curatedHistoryPlayerExtrasUseMovieIdAndTitleForDirectPlayback() {
        val extras = curatedHistoryPlayerExtras(movieId = "movie/ABC-001", title = "History Movie")

        assertEquals("movie/ABC-001", extras[CuratedPlayerContract.EXTRA_MOVIE_ID])
        assertEquals("History Movie", extras[CuratedPlayerContract.EXTRA_TITLE])
    }

    @Test
    fun playbackDescriptorResumePositionTakesPriorityOverStartPosition() {
        val descriptor =
            PlaybackDescriptor(
                movieId = "movie-1",
                mode = PlaybackMode.Direct,
                sessionId = null,
                sessionKind = null,
                url = "http://127.0.0.1:8081/api/library/movies/movie-1/stream",
                mimeType = null,
                fileName = null,
                transcodeProfile = null,
                durationSec = 7200.0,
                startPositionSec = 12.0,
                resumePositionSec = 42.5,
                canDirectPlay = true,
                reason = null,
                reasonCode = null,
                reasonMessage = null,
                sourceContainer = null,
                sourceVideoCodec = null,
                sourceAudioCodec = null,
            )

        assertEquals(42_500L, descriptor.curatedStartPositionMs())
    }

    @Test
    fun playbackDescriptorUsesResumePositionWhenStartPositionIsMissing() {
        val descriptor =
            PlaybackDescriptor(
                movieId = "movie-1",
                mode = PlaybackMode.Direct,
                sessionId = null,
                sessionKind = null,
                url = "http://127.0.0.1:8081/api/library/movies/movie-1/stream",
                mimeType = null,
                fileName = null,
                transcodeProfile = null,
                durationSec = 7200.0,
                startPositionSec = null,
                resumePositionSec = 7175.6,
                canDirectPlay = true,
                reason = null,
                reasonCode = null,
                reasonMessage = null,
                sourceContainer = null,
                sourceVideoCodec = null,
                sourceAudioCodec = null,
            )

        assertEquals(7_175_600L, descriptor.curatedStartPositionMs())
    }

    @Test
    fun playbackProgressUpdateConvertsPositivePositionAndDurationToSeconds() {
        val update =
            curatedPlaybackProgressUpdate(
                movieId = "movie-1",
                positionMs = 90_500L,
                durationMs = 7_200_000L,
            )

        assertEquals("movie-1", update?.movieId)
        assertEquals(90.5, update?.positionSec ?: -1.0, 0.0)
        assertEquals(7200.0, update?.durationSec ?: -1.0, 0.0)
    }

    @Test
    fun playbackProgressUpdateSkipsInvalidMovieOrPositionAndAllowsUnknownDuration() {
        assertEquals(null, curatedPlaybackProgressUpdate(movieId = "", positionMs = 90_500L, durationMs = 7_200_000L))
        assertEquals(null, curatedPlaybackProgressUpdate(movieId = "movie-1", positionMs = 0L, durationMs = 7_200_000L))

        val update =
            curatedPlaybackProgressUpdate(
                movieId = "movie-1",
                positionMs = 90_500L,
                durationMs = -1L,
            )

        assertEquals("movie-1", update?.movieId)
        assertEquals(90.5, update?.positionSec ?: -1.0, 0.0)
        assertEquals(null, update?.durationSec)
    }
}
