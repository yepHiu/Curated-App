package dev.curated.app.film.presentation.person

import dev.curated.app.models.FindroidMovie
import dev.curated.app.models.FindroidPerson
import dev.curated.app.models.FindroidShow

data class PersonState(
    val person: FindroidPerson? = null,
    val starredInMovies: List<FindroidMovie> = emptyList(),
    val starredInShows: List<FindroidShow> = emptyList(),
    val error: Exception? = null,
)
