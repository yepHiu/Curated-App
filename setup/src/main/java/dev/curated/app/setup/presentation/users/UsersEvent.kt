package dev.curated.app.setup.presentation.users

sealed interface UsersEvent {
    data object NavigateToHome : UsersEvent
}
