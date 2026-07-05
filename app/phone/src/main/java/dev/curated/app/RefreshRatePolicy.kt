package dev.curated.app

data class RefreshDisplayMode(
    val id: Int,
    val physicalWidth: Int,
    val physicalHeight: Int,
    val refreshRate: Float,
)

object RefreshRatePolicy {
    fun preferredMode(
        modes: List<RefreshDisplayMode>,
        currentMode: RefreshDisplayMode?,
    ): RefreshDisplayMode? {
        val validModes = modes.filter { it.id > 0 && it.refreshRate > 0f }
        if (validModes.isEmpty()) return null

        val currentResolutionModes =
            currentMode
                ?.takeIf { it.physicalWidth > 0 && it.physicalHeight > 0 }
                ?.let { current ->
                    validModes.filter {
                        it.physicalWidth == current.physicalWidth &&
                            it.physicalHeight == current.physicalHeight
                    }
                }
                .orEmpty()

        val candidates = currentResolutionModes.ifEmpty { validModes }
        return candidates.maxWith(
            compareBy<RefreshDisplayMode> { it.refreshRate }
                .thenBy { it.physicalWidth * it.physicalHeight }
                .thenBy { it.id }
        )
    }
}
