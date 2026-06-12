package dev.jdtech.jellyfin.curated.repository

import dev.jdtech.jellyfin.curated.api.CuratedApiClient
import dev.jdtech.jellyfin.curated.api.CuratedUrlResolver
import dev.jdtech.jellyfin.curated.api.MovieDetail
import dev.jdtech.jellyfin.curated.api.MoviesPage
import dev.jdtech.jellyfin.curated.api.PlaybackDescriptor
import dev.jdtech.jellyfin.curated.api.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class CuratedRepositoryImpl(
    baseUrl: String,
    client: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    json: Json = Json { ignoreUnknownKeys = true },
) : CuratedRepository {
    private val normalizedBaseUrl = CuratedUrlResolver.normalizeBaseUrl(baseUrl)
    private val api = CuratedApiClient(normalizedBaseUrl, client, json)

    override suspend fun getMovies(
        limit: Int,
        offset: Int,
        query: String?,
        actor: String?,
        studio: String?,
        mode: String?,
    ): MoviesPage =
        withContext(dispatcher) {
            api.getMovies(
                    limit = limit,
                    offset = offset,
                    query = query,
                    actor = actor,
                    studio = studio,
                    mode = mode,
                )
                .toDomain(normalizedBaseUrl)
        }

    override suspend fun getMovie(movieId: String): MovieDetail =
        withContext(dispatcher) { api.getMovie(movieId).toDomain(normalizedBaseUrl) }

    override suspend fun getPlaybackDescriptor(movieId: String): PlaybackDescriptor =
        withContext(dispatcher) { api.getPlaybackDescriptor(movieId).toDomain(normalizedBaseUrl) }
}
