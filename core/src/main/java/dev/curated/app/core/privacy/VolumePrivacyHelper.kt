package dev.curated.app.core.privacy

import android.app.Activity
import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.curated.app.settings.domain.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class VolumePrivacyHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
) : Application.ActivityLifecycleCallbacks {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val lifecycleTracker =
        ActivityPrivacyLifecycleTracker(
            onEnterForeground = { muteMediaVolume() },
            onExitForeground = { muteMediaVolume() },
        )
    private var registered = false

    fun start(application: Application) {
        if (registered) return

        registered = true
        application.registerActivityLifecycleCallbacks(this)
        muteMediaVolume()
        Timber.d("VolumePrivacyHelper registered on ActivityLifecycleCallbacks")
    }

    override fun onActivityStarted(activity: Activity) = lifecycleTracker.onActivityStarted()

    override fun onActivityStopped(activity: Activity) = lifecycleTracker.onActivityStopped()

    fun muteMediaVolume() {
        if (
            !PrivacyAudioPolicy.shouldMuteSystemMedia(
                autoMuteEnabled = appPreferences.getValue(appPreferences.privacyAutoMute)
            )
        ) {
            return
        }

        runCatching {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                0,
                0,
            )
        }.onFailure {
            Timber.w(it, "Failed to mute media volume")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) {
        if (PrivacyAudioPolicy.shouldMuteSystemMediaOnActivityPause()) {
            muteMediaVolume()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
