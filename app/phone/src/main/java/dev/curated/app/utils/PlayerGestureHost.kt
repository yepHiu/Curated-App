package dev.curated.app.utils

import android.content.ContentResolver
import android.view.Window
import dev.curated.app.PlayerGestureCapabilities
import dev.curated.app.databinding.ActivityPlayerBinding
import dev.curated.app.player.core.domain.models.PlayerChapter

interface PlayerGestureHost {
    val playerGestureBinding: ActivityPlayerBinding
    val playerGestureWindow: Window
    val playerGestureContentResolver: ContentResolver
    val playerGestureCapabilities: PlayerGestureCapabilities

    fun seekToPreviousChapterForGesture(): PlayerChapter? = null

    fun seekToNextChapterForGesture(): PlayerChapter? = null

    fun isLastChapterForGesture(): Boolean = false
}
