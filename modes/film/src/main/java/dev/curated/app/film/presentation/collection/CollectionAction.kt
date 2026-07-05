package dev.curated.app.film.presentation.collection

import dev.curated.app.models.FindroidItem

sealed interface CollectionAction {
    data class OnItemClick(val item: FindroidItem) : CollectionAction

    data object OnBackClick : CollectionAction
}
