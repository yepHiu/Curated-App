package dev.curated.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RefreshRatePolicyTest {
    @Test
    fun `prefers highest refresh rate at the current display resolution`() {
        val currentMode = displayMode(id = 1, width = 1080, height = 2400, refreshRate = 60f)

        val preferred =
            RefreshRatePolicy.preferredMode(
                modes =
                    listOf(
                        currentMode,
                        displayMode(id = 2, width = 1080, height = 2400, refreshRate = 120f),
                        displayMode(id = 3, width = 1440, height = 3200, refreshRate = 144f),
                    ),
                currentMode = currentMode,
            )

        assertEquals(2, preferred?.id)
    }

    @Test
    fun `falls back to highest valid refresh rate when current resolution is unavailable`() {
        val preferred =
            RefreshRatePolicy.preferredMode(
                modes =
                    listOf(
                        displayMode(id = 1, width = 1080, height = 2400, refreshRate = 60f),
                        displayMode(id = 2, width = 1080, height = 2400, refreshRate = 90f),
                        displayMode(id = 3, width = 1440, height = 3200, refreshRate = 120f),
                    ),
                currentMode = displayMode(id = 9, width = 720, height = 1600, refreshRate = 60f),
            )

        assertEquals(3, preferred?.id)
    }

    @Test
    fun `ignores modes with invalid ids or refresh rates`() {
        val preferred =
            RefreshRatePolicy.preferredMode(
                modes =
                    listOf(
                        displayMode(id = 0, refreshRate = 165f),
                        displayMode(id = 2, refreshRate = 0f),
                        displayMode(id = 3, refreshRate = 120f),
                    ),
                currentMode = null,
            )

        assertEquals(3, preferred?.id)
    }

    @Test
    fun `returns null when no valid display modes exist`() {
        val preferred =
            RefreshRatePolicy.preferredMode(
                modes =
                    listOf(
                        displayMode(id = 0, refreshRate = 120f),
                        displayMode(id = 2, refreshRate = 0f),
                    ),
                currentMode = null,
            )

        assertNull(preferred)
    }

    private fun displayMode(
        id: Int,
        width: Int = 1080,
        height: Int = 2400,
        refreshRate: Float,
    ) =
        RefreshDisplayMode(
            id = id,
            physicalWidth = width,
            physicalHeight = height,
            refreshRate = refreshRate,
        )
}
