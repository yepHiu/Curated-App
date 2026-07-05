package dev.curated.app.settings.presentation.settings

import dev.curated.app.settings.presentation.models.PreferenceGroup

data class SettingsState(
    val isLoading: Boolean = false,
    val preferenceGroups: List<PreferenceGroup> = emptyList(),
)
