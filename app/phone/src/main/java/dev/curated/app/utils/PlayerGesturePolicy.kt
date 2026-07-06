package dev.curated.app.utils

import kotlin.math.abs

private const val DRAG_DIRECTION_BIAS = 1.2f
private const val DEFAULT_SEEK_MS_PER_PIXEL = 90L

enum class PlayerDragGestureIntent {
    Undecided,
    HorizontalSeek,
    VerticalAdjustment,
}

data class PlayerDragSeekPreview(val offsetMs: Long, val targetPositionMs: Long)

fun isGestureStartInSystemGestureExclusionArea(
    startX: Float,
    startY: Float,
    viewWidth: Int,
    viewHeight: Int,
    leftInset: Int,
    topInset: Int,
    rightInset: Int,
    bottomInset: Int,
): Boolean {
    if (viewWidth <= 0 || viewHeight <= 0) {
        return false
    }

    return startX < leftInset ||
        startX > viewWidth - rightInset ||
        startY < topInset ||
        startY > viewHeight - bottomInset
}

fun detectPlayerDragGestureIntent(
    deltaX: Float,
    deltaY: Float,
    thresholdPx: Float,
): PlayerDragGestureIntent {
    val absX = abs(deltaX)
    val absY = abs(deltaY)

    return when {
        absX < thresholdPx && absY < thresholdPx -> PlayerDragGestureIntent.Undecided
        absX >= thresholdPx && absX >= absY * DRAG_DIRECTION_BIAS ->
            PlayerDragGestureIntent.HorizontalSeek
        absY >= thresholdPx && absY >= absX * DRAG_DIRECTION_BIAS ->
            PlayerDragGestureIntent.VerticalAdjustment
        else -> PlayerDragGestureIntent.Undecided
    }
}

fun calculatePlayerDragSeekPreview(
    startPositionMs: Long,
    durationMs: Long,
    deltaX: Float,
    seekMsPerPixel: Long = DEFAULT_SEEK_MS_PER_PIXEL,
): PlayerDragSeekPreview {
    val offsetMs = (deltaX * seekMsPerPixel).toLong()
    val unclampedTarget = (startPositionMs + offsetMs).coerceAtLeast(0L)
    val targetPositionMs =
        if (durationMs > 0L) {
            unclampedTarget.coerceAtMost(durationMs)
        } else {
            unclampedTarget
        }

    return PlayerDragSeekPreview(offsetMs = offsetMs, targetPositionMs = targetPositionMs)
}
