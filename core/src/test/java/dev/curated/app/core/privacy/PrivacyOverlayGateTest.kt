package dev.curated.app.core.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyOverlayGateTest {
    @Test
    fun threeTapsWithinTimeoutDismissesActiveOverlay() {
        val gate = PrivacyOverlayGate(tapTimeoutMs = 1_000L)

        gate.activate()

        assertFalse(gate.onTap(nowMs = 10_000L))
        assertFalse(gate.onTap(nowMs = 10_300L))
        assertTrue(gate.onTap(nowMs = 10_700L))
        assertFalse(gate.isActive)
    }

    @Test
    fun tapSequenceResetsAfterTimeout() {
        val gate = PrivacyOverlayGate(tapTimeoutMs = 1_000L)

        gate.activate()

        assertFalse(gate.onTap(nowMs = 10_000L))
        assertFalse(gate.onTap(nowMs = 11_200L))
        assertFalse(gate.onTap(nowMs = 11_500L))
        assertTrue(gate.isActive)
        assertTrue(gate.onTap(nowMs = 11_800L))
        assertFalse(gate.isActive)
    }
}
