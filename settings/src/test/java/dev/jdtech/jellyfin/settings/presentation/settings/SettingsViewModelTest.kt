package dev.jdtech.jellyfin.settings.presentation.settings

import dev.jdtech.jellyfin.settings.R
import dev.jdtech.jellyfin.settings.domain.models.Preference as BackendPreference
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceCategory
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceGroup
import dev.jdtech.jellyfin.settings.presentation.models.PreferenceSwitch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
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

    @Test
    fun curatedMvpHidesRemovedSettingsEntriesButKeepsExtraInfo() {
        assertTrue(isCuratedHiddenPreference(R.string.settings_preferred_audio_language))
        assertTrue(isCuratedHiddenPreference(R.string.settings_preferred_subtitle_language))
        assertTrue(isCuratedHiddenPreference(R.string.settings_category_interface))
        assertTrue(isCuratedHiddenPreference(R.string.trickplay))
        assertTrue(isCuratedHiddenPreference(R.string.pref_player_trickplay))
        assertTrue(isCuratedHiddenPreference(R.string.pref_player_gestures_seek_trickplay))

        assertFalse(isCuratedHiddenPreference(R.string.app_language))
        assertFalse(isCuratedHiddenPreference(R.string.extra_info))
        assertFalse(isCuratedHiddenPreference(R.string.settings_category_player))
    }

    @Test
    fun curatedMvpFiltersHiddenGroupsAndKeepsExtraInfoSwitch() {
        val visiblePreference = PreferenceSwitch(R.string.extra_info, backendPreference = booleanPref)
        val hiddenPreference =
            PreferenceSwitch(
                R.string.pref_player_trickplay,
                backendPreference = booleanPref,
            )
        val languageCategory =
            PreferenceCategory(
                nameStringResource = R.string.settings_category_language,
                nestedPreferenceGroups =
                    listOf(
                        PreferenceGroup(
                            preferences =
                                listOf(
                                    PreferenceSwitch(
                                        R.string.settings_preferred_audio_language,
                                        backendPreference = booleanPref,
                                    ),
                                    PreferenceSwitch(
                                        R.string.app_language,
                                        backendPreference = booleanPref,
                                    ),
                                )
                        )
                    ),
            )

        val filtered =
            curatedVisiblePreferenceGroups(
                listOf(
                    PreferenceGroup(
                        preferences =
                            listOf(
                                PreferenceCategory(R.string.settings_category_interface),
                                visiblePreference,
                                hiddenPreference,
                                languageCategory,
                            )
                    ),
                    PreferenceGroup(
                        nameStringResource = R.string.trickplay,
                        preferences = listOf(visiblePreference),
                    ),
                )
            )

        assertEquals(1, filtered.size)
        assertEquals(
            listOf(R.string.extra_info, R.string.settings_category_language),
            filtered.single().preferences.map { it.nameStringResource },
        )

        val filteredLanguage =
            filtered.single().preferences.filterIsInstance<PreferenceCategory>().single()
        assertEquals(
            listOf(R.string.app_language),
            filteredLanguage.nestedPreferenceGroups.single().preferences.map {
                it.nameStringResource
            },
        )
    }

    private companion object {
        val booleanPref = BackendPreference("test", false)
    }
}
