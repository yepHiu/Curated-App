package dev.curated.app.film.presentation.search

import dev.curated.app.models.FindroidItem

data class SearchState(val items: List<FindroidItem> = emptyList(), val loading: Boolean = false)
