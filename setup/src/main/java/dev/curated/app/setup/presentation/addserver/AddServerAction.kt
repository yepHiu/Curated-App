package dev.curated.app.setup.presentation.addserver

sealed interface AddServerAction {
    data class OnAddressChange(val address: String) : AddServerAction

    data class OnConnectClick(val address: String) : AddServerAction

    data object OnBackClick : AddServerAction
}
