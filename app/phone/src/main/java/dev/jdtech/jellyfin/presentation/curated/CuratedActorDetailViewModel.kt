package dev.jdtech.jellyfin.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.ActorProfile
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedActorDetailViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedActorDetailState())
    val state = _state.asStateFlow()
    private var actorName: String? = null
    private var loadActorJob: Job? = null
    private var loadNextMoviesJob: Job? = null

    fun loadActor(name: String) {
        actorName = name
        loadActorJob?.cancel()
        loadNextMoviesJob?.cancel()
        _state.value = CuratedActorDetailState(isLoading = true)
        loadActorJob =
            viewModelScope.launch {
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    val profile = repository.getActorProfile(name)
                    val movies =
                        CuratedActorMoviesPager(repository = repository, actorName = name)
                            .loadFirstPage()
                    _state.value =
                        CuratedActorDetailState(
                            isLoading = false,
                            profile = profile,
                            movies = movies,
                        )
                } catch (e: Exception) {
                    _state.value =
                        CuratedActorDetailState(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Unable to load actor",
                        )
                }
            }
    }

    fun loadNextMovies() {
        val name = actorName ?: return
        val current = _state.value
        if (!current.movies.canLoadMore) return

        _state.value =
            current.copy(
                movies = current.movies.copy(isLoadingMore = true, appendErrorMessage = null)
            )
        loadNextMoviesJob =
            viewModelScope.launch {
                val pageBase = current.movies.copy(isLoadingMore = false)
                try {
                    val repository = repositoryFactory.createForCurrentServer()
                    _state.value =
                        _state.value.copy(
                            movies =
                                CuratedActorMoviesPager(
                                        repository = repository,
                                        actorName = name,
                                    )
                                    .loadNextPage(pageBase)
                        )
                } catch (e: Exception) {
                    _state.value =
                        _state.value.copy(
                            movies =
                                _state.value.movies.copy(
                                    isLoadingMore = false,
                                    appendErrorMessage =
                                        e.localizedMessage ?: "Unable to load more movies",
                                )
                        )
                }
            }
    }
}

data class CuratedActorDetailState(
    val isLoading: Boolean = true,
    val profile: ActorProfile? = null,
    val movies: CuratedActorMoviesState = CuratedActorMoviesState(),
    val errorMessage: String? = null,
)
