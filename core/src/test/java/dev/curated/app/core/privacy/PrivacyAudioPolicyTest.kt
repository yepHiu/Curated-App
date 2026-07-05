package dev.curated.app.core.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyAudioPolicyTest {
    @Test
    fun systemMediaMuteFollowsAutoMuteSetting() {
        assertTrue(PrivacyAudioPolicy.shouldMuteSystemMedia(autoMuteEnabled = true))
        assertFalse(PrivacyAudioPolicy.shouldMuteSystemMedia(autoMuteEnabled = false))
    }

    @Test
    fun activityPauseDoesNotMuteSystemMediaDuringInAppNavigation() {
        assertFalse(PrivacyAudioPolicy.shouldMuteSystemMediaOnActivityPause())
    }

    @Test
    fun playerInternalMuteFollowsInternalMuteSetting() {
        assertTrue(PrivacyAudioPolicy.shouldMutePlayerAudio(playerInternalMuteEnabled = true))
        assertFalse(PrivacyAudioPolicy.shouldMutePlayerAudio(playerInternalMuteEnabled = false))
    }

    @Test
    fun activePlaybackKeepsPlayerAudibleWhenInternalMuteIsEnabled() {
        assertEquals(
            1f,
            PrivacyAudioPolicy.playerVolume(
                playerInternalMuteEnabled = true,
                privacyMuteActive = false,
            ),
            0f,
        )
    }

    @Test
    fun privacyMuteSilencesPlayerWhenInternalMuteIsEnabled() {
        assertEquals(
            0f,
            PrivacyAudioPolicy.playerVolume(
                playerInternalMuteEnabled = true,
                privacyMuteActive = true,
            ),
            0f,
        )
    }
}
