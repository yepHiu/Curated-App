package dev.jdtech.jellyfin.curated.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedAuthGateTest {
    @Test
    fun requiresUnlockOnlyWhenPinIsEnabledAndSessionIsLocked() {
        assertTrue(authStatus(pinEnabled = true, unlocked = false).requiresUnlock())
        assertFalse(authStatus(pinEnabled = false, unlocked = false).requiresUnlock())
        assertFalse(authStatus(pinEnabled = true, unlocked = true).requiresUnlock())
    }

    private fun authStatus(pinEnabled: Boolean, unlocked: Boolean): AuthStatusDto =
        AuthStatusDto(
            pinEnabled = pinEnabled,
            unlocked = unlocked,
            setupRequired = false,
            pinLength = 4,
            trustedForever = false,
            sessionTtlMinutes = 60,
            lanRequiresPin = false,
            lockOnRestart = false,
        )
}
