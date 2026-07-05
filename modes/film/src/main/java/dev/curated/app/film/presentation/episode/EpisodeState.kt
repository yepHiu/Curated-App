package dev.curated.app.film.presentation.episode

import dev.curated.app.models.FindroidEpisode
import dev.curated.app.models.FindroidItemPerson
import dev.curated.app.models.VideoMetadata

data class EpisodeState(
    val episode: FindroidEpisode? = null,
    val videoMetadata: VideoMetadata? = null,
    val actors: List<FindroidItemPerson> = emptyList(),
    val displayExtraInfo: Boolean = false,
    val error: Exception? = null,
)
