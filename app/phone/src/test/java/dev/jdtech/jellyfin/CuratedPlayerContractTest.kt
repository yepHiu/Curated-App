package dev.jdtech.jellyfin

import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.PlaybackMode
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
    fun playbackDescriptorStartPositionUsesMilliseconds() {
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

        assertEquals(12_000L, descriptor.curatedStartPositionMs())
    }

    @Test
    fun playbackDescriptorIgnoresResumePositionWhenStartPositionIsMissing() {
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

        assertEquals(0L, descriptor.curatedStartPositionMs())
    }
}
