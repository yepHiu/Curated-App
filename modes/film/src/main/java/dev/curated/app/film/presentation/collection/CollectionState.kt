package dev.curated.app.film.presentation.collection

import dev.curated.app.models.CollectionSection

data class CollectionState(
    val sections: List<CollectionSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: Exception? = null,
)
