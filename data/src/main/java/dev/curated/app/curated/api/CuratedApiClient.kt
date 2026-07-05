package dev.curated.app.curated.api

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CuratedApiClient(
    baseUrl: String,
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val normalizedBaseUrl = CuratedUrlResolver.normalizeBaseUrl(baseUrl)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getHealth(): HealthDto = get(path = "/health")

    fun getAuthStatus(): AuthStatusDto = get(path = "/auth/status")

    fun unlock(pin: String, trustedForever: Boolean): AuthStatusDto =
        post(path = "/auth/unlock", body = UnlockRequestDto(pin, trustedForever))

    fun getHomepageRecommendations(): HomepageDailyRecommendationsDto =
        get(path = "/homepage/recommendations")

    fun getMovies(
        limit: Int = 50,
        offset: Int = 0,
        query: String? = null,
        actor: String? = null,
        studio: String? = null,
        mode: String? = null,
    ): MoviesPageDto {
        val url =
            apiUrlBuilder("/library/movies")
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("offset", offset.toString())
                .addOptionalQueryParameter("q", query)
                .addOptionalQueryParameter("actor", actor)
                .addOptionalQueryParameter("studio", studio)
                .addOptionalQueryParameter("mode", mode)
                .build()
        return get(url)
    }

    fun getActors(
        limit: Int = 50,
        offset: Int = 0,
        query: String? = null,
        actorTag: String? = null,
        sort: String? = null,
    ): ActorsListDto {
        val url =
            apiUrlBuilder("/library/actors")
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("offset", offset.toString())
                .addOptionalQueryParameter("q", query)
                .addOptionalQueryParameter("actorTag", actorTag)
                .addOptionalQueryParameter("sort", sort)
                .build()
        return get(url)
    }

    fun getActorProfile(name: String): ActorProfileDto {
        val url =
            apiUrlBuilder("/library/actors/profile")
                .addQueryParameter("name", name)
                .build()
        return get(url)
    }

    fun getMovie(movieId: String): MovieDetailDto {
        val url = movieUrlBuilder(movieId).build()
        return get(url)
    }

    fun getPlaybackDescriptor(movieId: String): PlaybackDescriptorDto {
        val url = movieUrlBuilder(movieId).addPathSegment("playback").build()
        return get(url)
    }

    fun getPlaybackProgress(): PlaybackProgressListDto = get(path = "/playback/progress")

    fun updatePlaybackProgress(movieId: String, positionSec: Double, durationSec: Double?) {
        val url = apiUrlBuilder("/playback/progress").addPathSegment(movieId).build()
        val requestBody =
            json.encodeToString(
                    PlaybackProgressUpdateRequestDto(
                        positionSec = positionSec,
                        durationSec = durationSec,
                    )
                )
                .toRequestBody(jsonMediaType)
        val request = Request.Builder().url(url).put(requestBody).build()
        executeEmpty(request)
    }

    private inline fun <reified T> get(path: String): T {
        val request = baseRequest(path).get().build()
        return execute(request)
    }

    private inline fun <reified T> get(url: HttpUrl): T {
        val request = Request.Builder().url(url).get().build()
        return execute(request)
    }

    private inline fun <reified T, reified Body> post(path: String, body: Body): T {
        val requestBody = json.encodeToString(body).toRequestBody(jsonMediaType)
        val request = baseRequest(path).post(requestBody).build()
        return execute(request)
    }

    @Suppress("unused")
    private inline fun <reified Body> put(path: String, body: Body) {
        val requestBody = json.encodeToString(body).toRequestBody(jsonMediaType)
        val request = baseRequest(path).put(requestBody).build()
        executeEmpty(request)
    }

    private fun baseRequest(path: String): Request.Builder =
        Request.Builder().url(CuratedUrlResolver.apiUrl(normalizedBaseUrl, path))

    private fun apiUrlBuilder(path: String): HttpUrl.Builder =
        CuratedUrlResolver.apiUrl(normalizedBaseUrl, path).toHttpUrl().newBuilder()

    private fun movieUrlBuilder(movieId: String): HttpUrl.Builder =
        apiUrlBuilder("/library/movies").addPathSegment(movieId)

    private inline fun <reified T> execute(request: Request): T {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw CuratedApiException(
                    CuratedErrorMapper.map(statusCode = response.code, body = responseBody)
                )
            }
            return json.decodeFromString(responseBody)
        }
    }

    private fun executeEmpty(request: Request) {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw CuratedApiException(
                    CuratedErrorMapper.map(statusCode = response.code, body = responseBody)
                )
            }
        }
    }

    private fun HttpUrl.Builder.addOptionalQueryParameter(
        name: String,
        value: String?,
    ): HttpUrl.Builder {
        val trimmedValue = value?.trim()?.takeIf { it.isNotEmpty() } ?: return this
        return addQueryParameter(name, trimmedValue)
    }
}
