package dev.curated.app.curated.api

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val name: String,
    val version: String,
    val channel: String,
    val installerVersion: String,
    val transport: String,
    val databasePath: String,
)

@Serializable
data class AuthStatusDto(
    val pinEnabled: Boolean,
    val unlocked: Boolean,
    val setupRequired: Boolean,
    val pinLength: Int,
    val trustedForever: Boolean,
    val sessionTtlMinutes: Int,
    val lanRequiresPin: Boolean,
    val lockOnRestart: Boolean,
)

@Serializable
data class UnlockRequestDto(
    val pin: String,
    val trustedForever: Boolean,
)

@Serializable
data class MoviesPageDto(
    val items: List<MovieListItemDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)

@Serializable
data class HomepageDailyRecommendationsDto(
    val dateUtc: String,
    val generatedAt: String,
    val generationVersion: String,
    val heroMovieIds: List<String> = emptyList(),
    val recommendationMovieIds: List<String> = emptyList(),
)

@Serializable
data class ActorsListDto(
    val total: Int,
    val actors: List<ActorListItemDto> = emptyList(),
)

@Serializable
data class ActorListItemDto(
    val name: String,
    val avatarUrl: String? = null,
    val avatarRemoteUrl: String? = null,
    val avatarLocalUrl: String? = null,
    val hasLocalAvatar: Boolean = false,
    val movieCount: Int = 0,
    val userTags: List<String> = emptyList(),
)

@Serializable
data class ActorProfileDto(
    val name: String,
    val avatarUrl: String? = null,
    val avatarRemoteUrl: String? = null,
    val avatarLocalUrl: String? = null,
    val hasLocalAvatar: Boolean = false,
    val summary: String? = null,
    val homepage: String? = null,
    val provider: String? = null,
    val providerActorId: String? = null,
    val height: Int? = null,
    val birthday: String? = null,
    val profileUpdatedAt: String? = null,
    val userTags: List<String> = emptyList(),
    val externalLinks: List<String> = emptyList(),
)

@Serializable
data class MovieListItemDto(
    val id: String,
    val title: String,
    val code: String,
    val studio: String,
    val actors: List<String>,
    val tags: List<String>,
    val userTags: List<String> = emptyList(),
    val runtimeMinutes: Int,
    val rating: Double,
    val isFavorite: Boolean,
    val addedAt: String,
    val location: String,
    val resolution: String,
    val year: Int,
    val releaseDate: String? = null,
    val coverUrl: String? = null,
    val thumbUrl: String? = null,
    val trashedAt: String? = null,
)

@Serializable
data class MovieDetailDto(
    val id: String,
    val title: String,
    val code: String,
    val studio: String,
    val actors: List<String>,
    val tags: List<String>,
    val userTags: List<String> = emptyList(),
    val runtimeMinutes: Int,
    val rating: Double,
    val isFavorite: Boolean,
    val addedAt: String,
    val location: String,
    val resolution: String,
    val year: Int,
    val releaseDate: String? = null,
    val coverUrl: String? = null,
    val thumbUrl: String? = null,
    val trashedAt: String? = null,
    val summary: String,
    val previewImages: List<String> = emptyList(),
    val previewVideoUrl: String? = null,
    val metadataRating: Double,
    val userRating: Double? = null,
    val actorAvatarUrls: Map<String, String> = emptyMap(),
)

@Serializable
data class PlaybackDescriptorDto(
    val movieId: String,
    val mode: String,
    val sessionId: String? = null,
    val sessionKind: String? = null,
    val url: String,
    val mimeType: String? = null,
    val fileName: String? = null,
    val transcodeProfile: String? = null,
    val durationSec: Double? = null,
    val startPositionSec: Double? = null,
    val resumePositionSec: Double? = null,
    val canDirectPlay: Boolean,
    val reason: String? = null,
    val reasonCode: String? = null,
    val reasonMessage: String? = null,
    val sourceContainer: String? = null,
    val sourceVideoCodec: String? = null,
    val sourceAudioCodec: String? = null,
)

@Serializable
data class PlaybackProgressListDto(
    val items: List<PlaybackProgressDto>,
)

@Serializable
data class PlaybackProgressDto(
    val movieId: String,
    val positionSec: Double,
    val durationSec: Double? = null,
    val updatedAt: String,
)

@Serializable
data class PlaybackProgressUpdateRequestDto(
    val positionSec: Double,
    val durationSec: Double? = null,
)
