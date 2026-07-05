package dev.curated.app.settings.presentation.settings

import dev.curated.app.settings.presentation.models.Preference

sealed interface SettingsAction {
    data object OnBackClick : SettingsAction

    data class OnUpdate(val preference: Preference) : SettingsAction
}
