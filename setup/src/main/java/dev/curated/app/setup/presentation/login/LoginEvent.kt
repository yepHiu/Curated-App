package dev.curated.app.setup.presentation.login

sealed interface LoginEvent {
    data object Success : LoginEvent
}
