package dev.jdtech.jellyfin

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BaseApplicationStartupWorkTest {
    @Test
    fun curatedMvpDoesNotScheduleFindroidUserDataSync() {
        assertFalse(curatedMvpSchedulesFindroidUserDataSync())
    }

    @Test
    fun curatedMvpAlwaysStartsInDarkMode() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, curatedMvpDefaultNightMode())
    }
}
