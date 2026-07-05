package dev.curated.app.curated.repository

import dev.curated.app.curated.api.HomepageDailyRecommendations
import dev.curated.app.curated.api.ActorProfile
import dev.curated.app.curated.api.ActorsPage
import dev.curated.app.curated.api.MovieDetail
import dev.curated.app.curated.api.MoviesPage
import dev.curated.app.curated.api.PlaybackDescriptor
import dev.curated.app.curated.api.PlaybackProgress

interface CuratedRepository {
    suspend fun getHomepageRecommendations(): HomepageDailyRecommendations

    suspend fun getMovies(
        limit: Int = 50,
        offset: Int = 0,
        query: String? = null,
        actor: String? = null,
        studio: String? = null,
        mode: String? = null,
    ): MoviesPage

    suspend fun getActors(
        limit: Int = 50,
        offset: Int = 0,
        query: String? = null,
        actorTag: String? = null,
        sort: String? = null,
    ): ActorsPage

    suspend fun getActorProfile(name: String): ActorProfile

    suspend fun getMovie(movieId: String): MovieDetail

    suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor

    suspend fun getPlaybackProgress(): List<PlaybackProgress>

    suspend fun updatePlaybackProgress(
        movieId: String,
        positionSec: Double,
        durationSec: Double?,
    )
}
