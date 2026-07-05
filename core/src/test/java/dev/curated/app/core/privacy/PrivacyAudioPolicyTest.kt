package dev.curated.app.core.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyAudioPolicyTest {
    @Test
    fun systemMediaMuteFollowsAutoMuteSetting() {
        assertTrue(PrivacyAudioPolicy.shouldMuteSystemMedia(autoMuteEnabled = true))
        assertFalse(PrivacyAudioPolicy.shouldMuteSystemMedia(autoMuteEnabled = false))
    }

    @Test
    fun playerInternalMuteFollowsInternalMuteSetting() {
        assertTrue(PrivacyAudioPolicy.shouldMutePlayerAudio(playerInternalMuteEnabled = true))
        assertFalse(PrivacyAudioPolicy.shouldMutePlayerAudio(playerInternalMuteEnabled = false))
    }
}
