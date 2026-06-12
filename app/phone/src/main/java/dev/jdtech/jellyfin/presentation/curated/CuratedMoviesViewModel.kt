package dev.jdtech.jellyfin.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.Job
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

    init {
        loadMovies()
    }

    fun loadMovies() {
        loadMoviesJob?.cancel()
        loadNextPageJob?.cancel()
        _state.value = CuratedMoviesState(isLoading = true)
        loadMoviesJob =
            viewModelScope.launch {
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    _state.value = CuratedMoviesPager(repository).loadFirstPage()
                } catch (e: Exception) {
                    _state.value =
                        CuratedMoviesState(
                            isLoading = false,
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
                    _state.value = CuratedMoviesPager(repository).loadNextPage(pageBase)
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
}

data class CuratedMoviesState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val movies: List<MovieListItem> = emptyList(),
    val total: Int = 0,
    val errorMessage: String? = null,
    val appendErrorMessage: String? = null,
    val endReached: Boolean = false,
) {
    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && !endReached && movies.size < total
}
