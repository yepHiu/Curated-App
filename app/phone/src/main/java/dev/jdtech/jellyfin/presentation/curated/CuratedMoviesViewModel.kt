package dev.jdtech.jellyfin.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.MovieListItem
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedMoviesViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedMoviesState())
    val state = _state.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val page = repositoryFactory.createForCurrentServer().getMovies(limit = 50)
                _state.value =
                    CuratedMoviesState(
                        isLoading = false,
                        movies = page.items,
                        total = page.total,
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Unable to load movies",
                    )
            }
        }
    }
}

data class CuratedMoviesState(
    val isLoading: Boolean = true,
    val movies: List<MovieListItem> = emptyList(),
    val total: Int = 0,
    val errorMessage: String? = null,
)
