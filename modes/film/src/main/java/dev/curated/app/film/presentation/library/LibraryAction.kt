package dev.curated.app.film.presentation.library

import dev.curated.app.models.FindroidItem
import dev.curated.app.models.SortBy
import dev.curated.app.models.SortOrder

sealed interface LibraryAction {
    data class OnItemClick(val item: FindroidItem) : LibraryAction

    data object OnBackClick : LibraryAction

    data class ChangeSorting(val sortBy: SortBy, val sortOrder: SortOrder) : LibraryAction
}
