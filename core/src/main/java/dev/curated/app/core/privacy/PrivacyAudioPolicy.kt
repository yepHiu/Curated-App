package dev.curated.app.core.privacy

object PrivacyAudioPolicy {
    fun shouldMuteSystemMedia(autoMuteEnabled: Boolean): Boolean = autoMuteEnabled

    fun shouldMutePlayerAudio(playerInternalMuteEnabled: Boolean): Boolean =
        playerInternalMuteEnabled
}
