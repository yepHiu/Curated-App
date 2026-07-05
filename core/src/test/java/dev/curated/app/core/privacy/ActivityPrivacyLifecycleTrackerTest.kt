package dev.curated.app.core.privacy

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityPrivacyLifecycleTrackerTest {
    @Test
    fun foregroundAndBackgroundEdgesTriggerOnceAcrossMultipleActivities() {
        var foregroundCount = 0
        var backgroundCount = 0
        val tracker =
            ActivityPrivacyLifecycleTracker(
                onEnterForeground = { foregroundCount++ },
                onExitForeground = { backgroundCount++ },
            )

        tracker.onActivityStarted()
        tracker.onActivityStarted()
        tracker.onActivityStopped()
        tracker.onActivityStopped()

        assertEquals(1, foregroundCount)
        assertEquals(1, backgroundCount)
    }

    @Test
    fun unmatchedStopsDoNotCreateBackgroundTransitions() {
        var foregroundCount = 0
        var backgroundCount = 0
        val tracker =
            ActivityPrivacyLifecycleTracker(
                onEnterForeground = { foregroundCount++ },
                onExitForeground = { backgroundCount++ },
            )

        tracker.onActivityStopped()
        tracker.onActivityStarted()
        tracker.onActivityStopped()
        tracker.onActivityStopped()

        assertEquals(1, foregroundCount)
        assertEquals(1, backgroundCount)
    }
}
