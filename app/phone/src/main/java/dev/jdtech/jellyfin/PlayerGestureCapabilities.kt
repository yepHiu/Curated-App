package dev.jdtech.jellyfin

data class PlayerGestureCapabilities(val supportsChapterSkip: Boolean)

val defaultPlayerGestureCapabilities = PlayerGestureCapabilities(supportsChapterSkip = true)

val curatedPlayerGestureCapabilities = PlayerGestureCapabilities(supportsChapterSkip = false)

fun shouldHandleChapterSkipGesture(
    preferenceEnabled: Boolean,
    capabilities: PlayerGestureCapabilities,
): Boolean = preferenceEnabled && capabilities.supportsChapterSkip
