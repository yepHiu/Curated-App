package dev.jdtech.jellyfin.settings.presentation.settings

import dev.jdtech.jellyfin.settings.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {
    @Test
    fun curatedMvpHidesUnsupportedTopLevelSettings() {
        assertTrue(isCuratedHiddenPreference(R.string.offline_mode))
        assertTrue(isCuratedHiddenPreference(R.string.users))
        assertTrue(isCuratedHiddenPreference(R.string.title_download))

        assertFalse(isCuratedHiddenPreference(R.string.settings_category_servers))
        assertFalse(isCuratedHiddenPreference(R.string.settings_category_player))
        assertFalse(isCuratedHiddenPreference(R.string.about))
    }

    @Test
    fun curatedMvpHidesThemeAndDynamicColorSettings() {
        assertTrue(isCuratedHiddenPreference(R.string.theme))
        assertTrue(isCuratedHiddenPreference(R.string.dynamic_colors))

        assertFalse(isCuratedHiddenPreference(R.string.home))
        assertFalse(isCuratedHiddenPreference(R.string.extra_info))
    }
}
