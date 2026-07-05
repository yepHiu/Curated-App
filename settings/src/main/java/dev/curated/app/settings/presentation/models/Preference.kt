package dev.curated.app.settings.presentation.models

import dev.curated.app.settings.domain.models.Preference as PreferenceBackend
import dev.curated.app.settings.presentation.enums.DeviceType

interface Preference {
    val nameStringResource: Int
    val descriptionStringRes: Int?
    val iconDrawableId: Int?
    val enabled: Boolean
    val dependencies: List<PreferenceBackend<Boolean>>
    val supportedDeviceTypes: List<DeviceType>
}
