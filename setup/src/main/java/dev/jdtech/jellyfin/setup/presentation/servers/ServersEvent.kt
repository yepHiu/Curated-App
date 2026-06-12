package dev.jdtech.jellyfin.setup.presentation.servers

sealed interface ServersEvent {
    data object NavigateHome : ServersEvent

    data object NavigateAuthLock : ServersEvent

    data object AddressChanged : ServersEvent
}
