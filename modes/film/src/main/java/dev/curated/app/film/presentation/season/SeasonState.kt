package dev.curated.app.film.presentation.season

import dev.curated.app.models.FindroidEpisode
import dev.curated.app.models.FindroidSeason

data class SeasonState(
    val season: FindroidSeason? = null,
    val episodes: List<FindroidEpisode> = emptyList(),
    val error: Exception? = null,
)
