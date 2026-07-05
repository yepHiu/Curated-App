package dev.curated.app.setup.presentation.authlock

import dev.curated.app.models.UiText

data class AuthLockState(
    val pin: String = "",
    val pinLength: Int = 4,
    val trustedForever: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
)
