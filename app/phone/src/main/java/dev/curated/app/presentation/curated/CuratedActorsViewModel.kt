package dev.curated.app.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.curated.app.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedActorsViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedActorsState())
    val state = _state.asStateFlow()
    private var loadActorsJob: Job? = null
    private var loadNextPageJob: Job? = null
    private var searchQueryJob: Job? = null

    init {
        loadActors()
    }

    fun loadActors() {
        searchQueryJob?.cancel()
        loadActors(searchQuery = _state.value.searchQuery)
    }

    fun onSearchQueryChange(query: String) {
        if (query == _state.value.searchQuery) return

        searchQueryJob?.cancel()
        loadActorsJob?.cancel()
        loadNextPageJob?.cancel()

        _state.value = CuratedActorsState(isLoading = true, searchQuery = query)
        searchQueryJob =
            viewModelScope.launch {
                if (curatedActorsNormalizedSearchQuery(query) != null) {
                    delay(SEARCH_DEBOUNCE_MS)
                }
                loadActors(searchQuery = query)
            }
    }

    private fun loadActors(searchQuery: String) {
        loadActorsJob?.cancel()
        loadNextPageJob?.cancel()
        _state.value = CuratedActorsState(isLoading = true, searchQuery = searchQuery)
        loadActorsJob =
            viewModelScope.launch {
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    _state.value =
                        CuratedActorsPager(
                                repository = repository,
                                query = curatedActorsNormalizedSearchQuery(searchQuery),
                                sort = ACTOR_SORT,
                            )
                            .loadFirstPage(searchQuery = searchQuery)
                } catch (e: Exception) {
                    _state.value =
                        CuratedActorsState(
                            isLoading = false,
                            searchQuery = searchQuery,
                            errorMessage = e.localizedMessage ?: "Unable to load actors",
                        )
                }
            }
    }

    fun loadNextPage() {
        val current = _state.value
        if (!current.canLoadMore) return

        _state.value = current.copy(isLoadingMore = true, appendErrorMessage = null)
        loadNextPageJob =
            viewModelScope.launch {
                val pageBase = current.copy(isLoadingMore = false)
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    _state.value =
                        CuratedActorsPager(
                                repository = repository,
                                query = curatedActorsNormalizedSearchQuery(current.searchQuery),
                                sort = ACTOR_SORT,
                            )
                            .loadNextPage(pageBase)
                } catch (e: Exception) {
                    _state.value =
                        _state.value.copy(
                            isLoadingMore = false,
                            appendErrorMessage =
                                e.localizedMessage ?: "Unable to load more actors",
                        )
                }
            }
    }

    private companion object {
        const val ACTOR_SORT = "movieCount"
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
