package dev.curated.app.core.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPauseOverlayTrackerTest {
    @Test
    fun resumeAfterInAppActivitySwitchClearsPauseOverlay() {
        val tracker = PrivacyPauseOverlayTracker()
        val detailActivity = Any()

        tracker.markOverlayShownForPause(detailActivity)

        assertTrue(tracker.shouldHideOverlayOnResume(detailActivity))
    }

    @Test
    fun resumeAfterAppBackgroundKeepsPrivacyOverlay() {
        val tracker = PrivacyPauseOverlayTracker()
        val detailActivity = Any()

        tracker.markOverlayShownForPause(detailActivity)
        tracker.onAppExitedForeground()

        assertFalse(tracker.shouldHideOverlayOnResume(detailActivity))
    }

    @Test
    fun unmarkedActivityResumeDoesNotHideOverlay() {
        val tracker = PrivacyPauseOverlayTracker()

        assertFalse(tracker.shouldHideOverlayOnResume(Any()))
    }
}
