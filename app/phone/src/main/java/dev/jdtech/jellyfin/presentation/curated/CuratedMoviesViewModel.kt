package dev.jdtech.jellyfin.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedMoviesViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedMoviesState())
    val state = _state.asStateFlow()
    private var loadMoviesJob: Job? = null
    private var loadNextPageJob: Job? = null
    private var searchQueryJob: Job? = null

    init {
        loadMovies()
    }

    fun loadMovies() {
        searchQueryJob?.cancel()
        loadMovies(searchQuery = _state.value.searchQuery)
    }

    fun onSearchQueryChange(query: String) {
        if (query == _state.value.searchQuery) return

        searchQueryJob?.cancel()
        loadMoviesJob?.cancel()
        loadNextPageJob?.cancel()

        _state.value = CuratedMoviesState(isLoading = true, searchQuery = query)
        searchQueryJob =
            viewModelScope.launch {
                if (curatedMoviesNormalizedSearchQuery(query) != null) {
                    delay(SEARCH_DEBOUNCE_MS)
                }
                loadMovies(searchQuery = query)
            }
    }

    private fun loadMovies(searchQuery: String) {
        loadMoviesJob?.cancel()
        loadNextPageJob?.cancel()
        _state.value = CuratedMoviesState(isLoading = true, searchQuery = searchQuery)
        loadMoviesJob =
            viewModelScope.launch {
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    _state.value =
                        CuratedMoviesPager(
                                repository,
                                query = curatedMoviesNormalizedSearchQuery(searchQuery),
                            )
                            .loadFirstPage(searchQuery = searchQuery)
                } catch (e: Exception) {
                    _state.value =
                        CuratedMoviesState(
                            isLoading = false,
                            searchQuery = searchQuery,
                            errorMessage = e.localizedMessage ?: "Unable to load movies",
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
                        CuratedMoviesPager(
                                repository,
                                query = curatedMoviesNormalizedSearchQuery(current.searchQuery),
                            )
                            .loadNextPage(pageBase)
                } catch (e: Exception) {
                    _state.value =
                        _state.value.copy(
                            isLoadingMore = false,
                            appendErrorMessage =
                                e.localizedMessage ?: "Unable to load more movies",
                        )
                }
            }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

data class CuratedMoviesState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val movies: List<MovieListItem> = emptyList(),
    val total: Int = 0,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val appendErrorMessage: String? = null,
    val endReached: Boolean = false,
) {
    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && !endReached && movies.size < total
}
