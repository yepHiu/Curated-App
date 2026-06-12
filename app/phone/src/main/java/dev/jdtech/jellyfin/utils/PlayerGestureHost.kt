package dev.jdtech.jellyfin.utils

import android.content.ContentResolver
import android.view.Window
import dev.jdtech.jellyfin.PlayerGestureCapabilities
import dev.jdtech.jellyfin.databinding.ActivityPlayerBinding
import dev.jdtech.jellyfin.player.core.domain.models.PlayerChapter

interface PlayerGestureHost {
    val playerGestureBinding: ActivityPlayerBinding
    val playerGestureWindow: Window
    val playerGestureContentResolver: ContentResolver
    val playerGestureCapabilities: PlayerGestureCapabilities

    fun seekToPreviousChapterForGesture(): PlayerChapter? = null

    fun seekToNextChapterForGesture(): PlayerChapter? = null

    fun isLastChapterForGesture(): Boolean = false
}
