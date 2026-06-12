package dev.jdtech.jellyfin.setup.presentation.authlock

sealed interface AuthLockEvent {
    data object Success : AuthLockEvent

    data object ChangeServer : AuthLockEvent
}
