package dev.curated.app.core.presentation.dummy

import dev.curated.app.models.AudioChannel
import dev.curated.app.models.AudioCodec
import dev.curated.app.models.DisplayProfile
import dev.curated.app.models.Resolution
import dev.curated.app.models.VideoCodec
import dev.curated.app.models.VideoMetadata

val dummyVideoMetadata =
    VideoMetadata(
        size = 1000000000,
        videoTracks = emptyList(),
        audioTracks = emptyList(),
        subtitleTracks = emptyList(),
        resolution = listOf(Resolution.HD),
        videoCodecs = listOf(VideoCodec.AV1),
        displayProfiles = listOf(DisplayProfile.HDR10),
        audioChannels = listOf(AudioChannel.CH_5_1),
        audioCodecs = listOf(AudioCodec.OPUS),
        isAtmos = listOf(false),
    )
