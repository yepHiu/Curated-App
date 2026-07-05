package dev.curated.app.film.presentation.search

import dev.curated.app.models.FindroidItem

sealed interface SearchAction {
    data class Search(val query: String) : SearchAction

    data class OnItemClick(val item: FindroidItem) : SearchAction
}
