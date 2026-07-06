package dev.curated.app

import dev.curated.app.utils.PlayerDragGestureIntent
import dev.curated.app.utils.calculatePlayerDragSeekPreview
import dev.curated.app.utils.detectPlayerDragGestureIntent
import dev.curated.app.utils.isGestureStartInSystemGestureExclusionArea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGestureExclusionPolicyTest {
    @Test
    fun landscapeRightHalfIsNotExcludedWhenViewIsWiderThanPortraitDisplayMetric() {
        assertFalse(
            isGestureStartInSystemGestureExclusionArea(
                startX = 1_700f,
                startY = 500f,
                viewWidth = 2_400,
                viewHeight = 1_080,
                leftInset = 48,
                topInset = 0,
                rightInset = 48,
                bottomInset = 0,
            )
        )
    }

    @Test
    fun systemGestureEdgesRemainExcluded() {
        assertTrue(
            isGestureStartInSystemGestureExclusionArea(
                startX = 2_370f,
                startY = 500f,
                viewWidth = 2_400,
                viewHeight = 1_080,
                leftInset = 48,
                topInset = 0,
                rightInset = 48,
                bottomInset = 0,
            )
        )
    }

    @Test
    fun horizontalDominantDragStartsSeekGesture() {
        assertEquals(
            PlayerDragGestureIntent.HorizontalSeek,
            detectPlayerDragGestureIntent(deltaX = 72f, deltaY = 18f, thresholdPx = 50f),
        )
    }

    @Test
    fun verticalDominantDragStartsVolumeOrBrightnessGesture() {
        assertEquals(
            PlayerDragGestureIntent.VerticalAdjustment,
            detectPlayerDragGestureIntent(deltaX = 12f, deltaY = -72f, thresholdPx = 50f),
        )
    }

    @Test
    fun diagonalDragWaitsForClearIntentBeforeHijackingPlayback() {
        assertEquals(
            PlayerDragGestureIntent.Undecided,
            detectPlayerDragGestureIntent(deltaX = 55f, deltaY = 50f, thresholdPx = 50f),
        )
    }

    @Test
    fun horizontalDragSeekPreviewUsesBilibiliStyleScreenScrubAndClampsToDuration() {
        val preview =
            calculatePlayerDragSeekPreview(
                startPositionMs = 120_000L,
                durationMs = 180_000L,
                deltaX = 1_000f,
            )

        assertEquals(90_000L, preview.offsetMs)
        assertEquals(180_000L, preview.targetPositionMs)
    }

    @Test
    fun horizontalDragSeekPreviewDoesNotSnapToZeroWhenDurationIsUnknown() {
        val preview =
            calculatePlayerDragSeekPreview(
                startPositionMs = 120_000L,
                durationMs = 0L,
                deltaX = 100f,
            )

        assertEquals(9_000L, preview.offsetMs)
        assertEquals(129_000L, preview.targetPositionMs)
    }
}
