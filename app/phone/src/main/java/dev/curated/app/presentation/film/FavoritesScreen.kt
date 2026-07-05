package dev.curated.app.presentation.film

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.curated.app.core.R as CoreR
import dev.curated.app.core.presentation.dummy.dummyMovies
import dev.curated.app.film.presentation.collection.CollectionAction
import dev.curated.app.film.presentation.collection.CollectionState
import dev.curated.app.film.presentation.favorites.FavoritesViewModel
import dev.curated.app.models.CollectionSection
import dev.curated.app.models.FindroidItem
import dev.curated.app.models.UiText
import dev.curated.app.presentation.theme.CuratedTheme

@Composable
fun FavoritesScreen(
    onItemClick: (item: FindroidItem) -> Unit,
    navigateBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadItems() }

    CollectionScreenLayout(
        collectionName = stringResource(CoreR.string.title_favorite),
        state = state,
        onAction = { action ->
            when (action) {
                is CollectionAction.OnItemClick -> onItemClick(action.item)
                is CollectionAction.OnBackClick -> navigateBack()
            }
        },
    )
}

@PreviewScreenSizes
@Composable
private fun CollectionScreenLayoutPreview() {
    CuratedTheme {
        CollectionScreenLayout(
            collectionName = "Favorites",
            state =
                CollectionState(
                    sections =
                        listOf(
                            CollectionSection(
                                id = 0,
                                name = UiText.StringResource(CoreR.string.title_favorite),
                                items = dummyMovies,
                            )
                        )
                ),
            onAction = {},
        )
    }
}
