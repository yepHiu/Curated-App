package dev.jdtech.jellyfin.curated.api

data class MovieListItem(
    val id: String,
    val title: String,
    val code: String,
    val studio: String,
    val actors: List<String>,
    val tags: List<String>,
    val userTags: List<String>,
    val runtimeMinutes: Int,
    val rating: Double,
    val isFavorite: Boolean,
    val addedAt: String,
    val location: String,
    val resolution: String,
    val year: Int,
    val releaseDate: String?,
    val coverUrl: String?,
    val thumbUrl: String?,
    val trashedAt: String?,
)

data class MoviesPage(
    val items: List<MovieListItem>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)

data class MovieDetail(
    val id: String,
    val title: String,
    val code: String,
    val studio: String,
    val actors: List<String>,
    val tags: List<String>,
    val userTags: List<String>,
    val runtimeMinutes: Int,
    val rating: Double,
    val isFavorite: Boolean,
    val addedAt: String,
    val location: String,
    val resolution: String,
    val year: Int,
    val releaseDate: String?,
    val coverUrl: String?,
    val thumbUrl: String?,
    val trashedAt: String?,
    val summary: String,
    val previewImages: List<String>,
    val previewVideoUrl: String?,
    val metadataRating: Double,
    val userRating: Double?,
    val actorAvatarUrls: Map<String, String>,
)

enum class PlaybackMode {
    Direct,
    Hls,
    Native,
}

data class PlaybackDescriptor(
    val movieId: String,
    val mode: PlaybackMode,
    val sessionId: String?,
    val sessionKind: String?,
    val url: String,
    val mimeType: String?,
    val fileName: String?,
    val transcodeProfile: String?,
    val durationSec: Double?,
    val startPositionSec: Double?,
    val resumePositionSec: Double?,
    val canDirectPlay: Boolean,
    val reason: String?,
    val reasonCode: String?,
    val reasonMessage: String?,
    val sourceContainer: String?,
    val sourceVideoCodec: String?,
    val sourceAudioCodec: String?,
)
