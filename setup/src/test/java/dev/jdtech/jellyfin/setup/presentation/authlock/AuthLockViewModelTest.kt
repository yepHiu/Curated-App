package dev.jdtech.jellyfin.setup.presentation.authlock

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthLockViewModelTest {
    @Test
    fun sanitizeCuratedPinInputKeepsDigitsAndLimitsLength() {
        assertEquals("1234", sanitizeCuratedPinInput("12a3 4-5", maxLength = 4))
    }

    @Test
    fun sanitizeCuratedPinInputAllowsEmptyInput() {
        assertEquals("", sanitizeCuratedPinInput("", maxLength = 4))
    }
}
