package dev.curated.app.core.presentation.dummy

import dev.curated.app.models.CollectionType
import dev.curated.app.models.FindroidCollection
import dev.curated.app.models.FindroidImages
import java.util.UUID

private val dummyMoviesCollection =
    FindroidCollection(
        id = UUID.randomUUID(),
        name = "Movies",
        type = CollectionType.Movies,
        images = FindroidImages(),
    )

private val dummyShowsCollection =
    FindroidCollection(
        id = UUID.randomUUID(),
        name = "Shows",
        type = CollectionType.TvShows,
        images = FindroidImages(),
    )

val dummyCollections = listOf(dummyMoviesCollection, dummyShowsCollection)
