package dev.jdtech.jellyfin.presentation.curated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.repository.CuratedRepository
import dev.jdtech.jellyfin.curated.repository.CuratedRepositoryFactory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CuratedHomeViewModel
@Inject
constructor(private val repositoryFactory: CuratedRepositoryFactory) : ViewModel() {
    private val _state = MutableStateFlow(CuratedHomeState())
    val state = _state.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        _state.value = CuratedHomeState(isLoading = true)
        viewModelScope.launch {
            try {
                val repository = repositoryFactory.createForCurrentServer()
                val content = CuratedHomeLoader(repository).load()
                _state.value =
                    CuratedHomeState(
                        isLoading = false,
                        heroMovies = content.heroMovies,
                        todayRecommendations = content.todayRecommendations,
                        dateUtc = content.dateUtc,
                        generatedAt = content.generatedAt,
                    )
            } catch (e: Exception) {
                _state.value =
                    CuratedHomeState(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Unable to load home",
                    )
            }
        }
    }
}

data class CuratedHomeState(
    val isLoading: Boolean = true,
    val heroMovies: List<MovieDetail> = emptyList(),
    val todayRecommendations: List<MovieDetail> = emptyList(),
    val dateUtc: String? = null,
    val generatedAt: String? = null,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && heroMovies.isEmpty() && todayRecommendations.isEmpty()
}

data class CuratedHomeContent(
    val dateUtc: String,
    val generatedAt: String,
    val heroMovies: List<MovieDetail>,
    val todayRecommendations: List<MovieDetail>,
)

internal class CuratedHomeLoader(private val repository: CuratedRepository) {
    suspend fun load(): CuratedHomeContent {
        val recommendations = repository.getHomepageRecommendations()
        val heroIds = recommendations.heroMovieIds.normalizedDistinctIds()
        val recommendationIds = recommendations.recommendationMovieIds.normalizedDistinctIds()
        val movieIds = (heroIds + recommendationIds).distinct()

        val moviesById = linkedMapOf<String, MovieDetail>()
        movieIds.forEach { movieId ->
            runCatching { repository.getMovie(movieId) }.getOrNull()?.let { movie ->
                moviesById[movieId] = movie
            }
        }

        return CuratedHomeContent(
            dateUtc = recommendations.dateUtc,
            generatedAt = recommendations.generatedAt,
            heroMovies = heroIds.mapNotNull { moviesById[it] },
            todayRecommendations = recommendationIds.mapNotNull { moviesById[it] },
        )
    }

    private fun List<String>.normalizedDistinctIds(): List<String> =
        map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}
