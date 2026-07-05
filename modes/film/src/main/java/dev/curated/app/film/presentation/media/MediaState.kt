package dev.curated.app.film.presentation.media

import dev.curated.app.models.FindroidCollection

data class MediaState(
    val libraries: List<FindroidCollection> = emptyList(),
    val isLoading: Boolean = false,
    val error: Exception? = null,
)
