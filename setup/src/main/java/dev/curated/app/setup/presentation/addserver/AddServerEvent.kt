package dev.curated.app.setup.presentation.addserver

sealed interface AddServerEvent {
    data object NavigateHome : AddServerEvent

    data object NavigateAuthLock : AddServerEvent
}
