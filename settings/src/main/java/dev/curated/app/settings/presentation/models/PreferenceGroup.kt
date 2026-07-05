package dev.curated.app.settings.presentation.models

import androidx.annotation.StringRes

data class PreferenceGroup(
    @param:StringRes val nameStringResource: Int? = null,
    val preferences: List<Preference>,
)
