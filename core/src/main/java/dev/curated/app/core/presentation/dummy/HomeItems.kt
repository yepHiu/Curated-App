package dev.curated.app.core.presentation.dummy

import dev.curated.app.models.CollectionType
import dev.curated.app.models.HomeItem
import dev.curated.app.models.HomeSection
import dev.curated.app.models.UiText
import dev.curated.app.models.View
import java.util.UUID

val dummyHomeSuggestions = HomeItem.Suggestions(id = UUID.randomUUID(), items = dummyMovies)

val dummyHomeSection =
    HomeItem.Section(
        HomeSection(
            id = UUID.randomUUID(),
            name = UiText.DynamicString("Continue watching"),
            items = dummyMovies + dummyEpisodes,
        )
    )

val dummyHomeView =
    HomeItem.ViewItem(
        View(
            id = UUID.randomUUID(),
            name = "Movies",
            items = dummyMovies,
            type = CollectionType.Movies,
        )
    )
