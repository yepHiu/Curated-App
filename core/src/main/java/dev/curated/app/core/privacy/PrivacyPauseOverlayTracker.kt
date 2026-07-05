package dev.curated.app.core.privacy

class PrivacyPauseOverlayTracker {
    private val pauseOverlayGenerations = mutableMapOf<Any, Int>()
    private var foregroundExitGeneration = 0

    fun markOverlayShownForPause(owner: Any) {
        pauseOverlayGenerations[owner] = foregroundExitGeneration
    }

    fun onAppExitedForeground() {
        foregroundExitGeneration++
    }

    fun shouldHideOverlayOnResume(owner: Any): Boolean {
        val pauseGeneration = pauseOverlayGenerations.remove(owner) ?: return false
        return pauseGeneration == foregroundExitGeneration
    }

    fun clear(owner: Any) {
        pauseOverlayGenerations.remove(owner)
    }
}
