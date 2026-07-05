package dev.curated.app.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedMovieDetailViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedMovieDetailState())
    val state = _state.asStateFlow()

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _state.value = CuratedMovieDetailState(isLoading = true)
            try {
                val movie = repositoryFactory.createForCurrentServer().getMovie(movieId)
                _state.value = CuratedMovieDetailState(isLoading = false, movie = movie)
            } catch (e: Exception) {
                _state.value =
                    CuratedMovieDetailState(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Unable to load movie",
                    )
            }
        }
    }
}

data class CuratedMovieDetailState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val errorMessage: String? = null,
)
