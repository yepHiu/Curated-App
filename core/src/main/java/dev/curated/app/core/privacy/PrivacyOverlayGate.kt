package dev.curated.app.core.privacy

private const val DEFAULT_TAP_TIMEOUT_MS = 1_000L

class PrivacyOverlayGate(
    private val tapTimeoutMs: Long = DEFAULT_TAP_TIMEOUT_MS,
) {
    var isActive: Boolean = false
        private set

    private var tapCount = 0
    private var firstTapTimeMs = 0L

    fun activate() {
        isActive = true
        resetTaps()
    }

    fun dismiss() {
        isActive = false
        resetTaps()
    }

    fun onTap(nowMs: Long): Boolean {
        if (!isActive) return false

        if (tapCount == 0 || nowMs - firstTapTimeMs > tapTimeoutMs) {
            tapCount = 1
            firstTapTimeMs = nowMs
            return false
        }

        tapCount++
        if (tapCount < 3) {
            return false
        }

        dismiss()
        return true
    }

    private fun resetTaps() {
        tapCount = 0
        firstTapTimeMs = 0L
    }
}
