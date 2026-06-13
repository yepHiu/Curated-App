package dev.jdtech.jellyfin.curated.api

fun MovieListItemDto.toDomain(baseUrl: String): MovieListItem =
    MovieListItem(
        id = id,
        title = title,
        code = code,
        studio = studio,
        actors = actors,
        tags = tags,
        userTags = userTags,
        runtimeMinutes = runtimeMinutes,
        rating = rating,
        isFavorite = isFavorite,
        addedAt = addedAt,
        location = location,
        resolution = resolution,
        year = year,
        releaseDate = releaseDate,
        coverUrl = CuratedUrlResolver.absoluteUrl(baseUrl, coverUrl),
        thumbUrl = CuratedUrlResolver.absoluteUrl(baseUrl, thumbUrl),
        trashedAt = trashedAt,
    )

fun MoviesPageDto.toDomain(baseUrl: String): MoviesPage =
    MoviesPage(
        items = items.map { it.toDomain(baseUrl) },
        total = total,
        limit = limit,
        offset = offset,
    )

fun HomepageDailyRecommendationsDto.toDomain(): HomepageDailyRecommendations =
    HomepageDailyRecommendations(
        dateUtc = dateUtc,
        generatedAt = generatedAt,
        generationVersion = generationVersion,
        heroMovieIds = heroMovieIds,
        recommendationMovieIds = recommendationMovieIds,
    )

fun ActorListItemDto.toDomain(baseUrl: String): ActorListItem =
    ActorListItem(
        name = name,
        avatarUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarUrl),
        avatarRemoteUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarRemoteUrl),
        avatarLocalUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarLocalUrl),
        hasLocalAvatar = hasLocalAvatar,
        movieCount = movieCount,
        userTags = userTags,
    )

fun ActorsListDto.toDomain(baseUrl: String): ActorsPage =
    ActorsPage(
        actors = actors.map { it.toDomain(baseUrl) },
        total = total,
    )

fun ActorProfileDto.toDomain(baseUrl: String): ActorProfile =
    ActorProfile(
        name = name,
        avatarUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarUrl),
        avatarRemoteUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarRemoteUrl),
        avatarLocalUrl = CuratedUrlResolver.absoluteUrl(baseUrl, avatarLocalUrl),
        hasLocalAvatar = hasLocalAvatar,
        summary = summary,
        homepage = homepage,
        provider = provider,
        providerActorId = providerActorId,
        height = height,
        birthday = birthday,
        profileUpdatedAt = profileUpdatedAt,
        userTags = userTags,
        externalLinks = externalLinks,
    )

fun MovieDetailDto.toDomain(baseUrl: String): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        code = code,
        studio = studio,
        actors = actors,
        tags = tags,
        userTags = userTags,
        runtimeMinutes = runtimeMinutes,
        rating = rating,
        isFavorite = isFavorite,
        addedAt = addedAt,
        location = location,
        resolution = resolution,
        year = year,
        releaseDate = releaseDate,
        coverUrl = CuratedUrlResolver.absoluteUrl(baseUrl, coverUrl),
        thumbUrl = CuratedUrlResolver.absoluteUrl(baseUrl, thumbUrl),
        trashedAt = trashedAt,
        summary = summary,
        previewImages = previewImages.mapNotNull { CuratedUrlResolver.absoluteUrl(baseUrl, it) },
        previewVideoUrl = CuratedUrlResolver.absoluteUrl(baseUrl, previewVideoUrl),
        metadataRating = metadataRating,
        userRating = userRating,
        actorAvatarUrls =
            actorAvatarUrls.mapValues { (_, url) ->
                CuratedUrlResolver.absoluteUrl(baseUrl, url).orEmpty()
            },
    )

fun PlaybackDescriptorDto.toDomain(baseUrl: String): PlaybackDescriptor =
    PlaybackDescriptor(
        movieId = movieId,
        mode = mode.toPlaybackMode(),
        sessionId = sessionId,
        sessionKind = sessionKind,
        url = CuratedUrlResolver.absoluteUrl(baseUrl, url).orEmpty(),
        mimeType = mimeType,
        fileName = fileName,
        transcodeProfile = transcodeProfile,
        durationSec = durationSec,
        startPositionSec = startPositionSec,
        resumePositionSec = resumePositionSec,
        canDirectPlay = canDirectPlay,
        reason = reason,
        reasonCode = reasonCode,
        reasonMessage = reasonMessage,
        sourceContainer = sourceContainer,
        sourceVideoCodec = sourceVideoCodec,
        sourceAudioCodec = sourceAudioCodec,
    )

fun PlaybackProgressDto.toDomain(): PlaybackProgress =
    PlaybackProgress(
        movieId = movieId,
        positionSec = positionSec,
        durationSec = durationSec,
        updatedAt = updatedAt,
    )

private fun String.toPlaybackMode(): PlaybackMode =
    when (lowercase()) {
        "direct" -> PlaybackMode.Direct
        "hls" -> PlaybackMode.Hls
        "native" -> PlaybackMode.Native
        else -> PlaybackMode.Direct
    }
