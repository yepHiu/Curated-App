package dev.curated.app.core.privacy

class ActivityPrivacyLifecycleTracker(
    private val onEnterForeground: () -> Unit,
    private val onExitForeground: () -> Unit,
) {
    private var startedActivityCount = 0

    fun onActivityStarted() {
        if (startedActivityCount == 0) {
            onEnterForeground()
        }
        startedActivityCount++
    }

    fun onActivityStopped() {
        if (startedActivityCount == 0) {
            return
        }

        startedActivityCount--
        if (startedActivityCount == 0) {
            onExitForeground()
        }
    }
}
