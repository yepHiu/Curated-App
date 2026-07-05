package dev.curated.app.setup.presentation.welcome

sealed interface WelcomeAction {
    data object OnContinueClick : WelcomeAction
}
