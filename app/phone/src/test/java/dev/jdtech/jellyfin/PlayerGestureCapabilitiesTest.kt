package dev.jdtech.jellyfin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGestureCapabilitiesTest {
    @Test
    fun defaultPlayerGesturesCanUseChapterSkipWhenPreferenceIsEnabled() {
        assertTrue(
            shouldHandleChapterSkipGesture(
                preferenceEnabled = true,
                capabilities = defaultPlayerGestureCapabilities,
            )
        )
    }

    @Test
    fun curatedPlayerGesturesDoNotUseChapterSkipEvenWhenPreferenceIsEnabled() {
        assertFalse(
            shouldHandleChapterSkipGesture(
                preferenceEnabled = true,
                capabilities = curatedPlayerGestureCapabilities,
            )
        )
    }

    @Test
    fun defaultPlayerGesturesDoNotUseChapterSkipWhenPreferenceIsDisabled() {
        assertFalse(
            shouldHandleChapterSkipGesture(
                preferenceEnabled = false,
                capabilities = defaultPlayerGestureCapabilities,
            )
        )
    }
}
