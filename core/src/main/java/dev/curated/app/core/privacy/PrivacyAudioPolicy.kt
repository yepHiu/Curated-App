package dev.curated.app.core.privacy

object PrivacyAudioPolicy {
    const val PlayerAudibleVolume = 1f
    const val PlayerMutedVolume = 0f

    fun shouldMuteSystemMedia(autoMuteEnabled: Boolean): Boolean = autoMuteEnabled

    fun shouldMuteSystemMediaOnActivityPause(): Boolean = false

    fun shouldMutePlayerAudio(playerInternalMuteEnabled: Boolean): Boolean =
        playerInternalMuteEnabled

    fun playerVolume(
        playerInternalMuteEnabled: Boolean,
        privacyMuteActive: Boolean,
    ): Float =
        if (playerInternalMuteEnabled && privacyMuteActive) {
            PlayerMutedVolume
        } else {
            PlayerAudibleVolume
        }
}
