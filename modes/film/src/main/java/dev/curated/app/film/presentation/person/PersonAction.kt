package dev.curated.app.film.presentation.person

import dev.curated.app.models.FindroidItem

sealed interface PersonAction {
    data object NavigateBack : PersonAction

    data object NavigateHome : PersonAction

    data class NavigateToItem(val item: FindroidItem) : PersonAction
}
