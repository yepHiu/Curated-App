package dev.curated.app.film.presentation.show

import dev.curated.app.models.FindroidEpisode
import dev.curated.app.models.FindroidItemPerson
import dev.curated.app.models.FindroidSeason
import dev.curated.app.models.FindroidShow

data class ShowState(
    val show: FindroidShow? = null,
    val nextUp: FindroidEpisode? = null,
    val seasons: List<FindroidSeason> = emptyList(),
    val actors: List<FindroidItemPerson> = emptyList(),
    val director: FindroidItemPerson? = null,
    val writers: List<FindroidItemPerson> = emptyList(),
    val error: Exception? = null,
)
