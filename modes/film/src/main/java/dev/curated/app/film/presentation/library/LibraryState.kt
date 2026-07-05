package dev.curated.app.film.presentation.library

import androidx.paging.PagingData
import dev.curated.app.models.FindroidItem
import dev.curated.app.models.SortBy
import dev.curated.app.models.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class LibraryState(
    val items: Flow<PagingData<FindroidItem>> = emptyFlow(),
    val sortBy: SortBy = SortBy.NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val isLoading: Boolean = false,
    val error: Exception? = null,
)
