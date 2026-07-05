package dev.curated.app.film.presentation.home

import dev.curated.app.models.HomeItem
import dev.curated.app.models.Server

data class HomeState(
    val server: Server? = null,
    val suggestionsSection: HomeItem.Suggestions? = null,
    val resumeSection: HomeItem.Section? = null,
    val nextUpSection: HomeItem.Section? = null,
    val views: List<HomeItem.ViewItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: Exception? = null,
)
