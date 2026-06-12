package dev.jdtech.jellyfin.setup.presentation.authlock

sealed interface AuthLockAction {
    data class OnPinChange(val pin: String) : AuthLockAction

    data class OnTrustedForeverChange(val trustedForever: Boolean) : AuthLockAction

    data object OnUnlockClick : AuthLockAction

    data object OnChangeServerClick : AuthLockAction
}
