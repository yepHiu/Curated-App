package dev.curated.app.core.privacy

import android.media.AudioManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * 音量隐私助手：App 在前台进入 / 后台退出时自动将媒体音量归零。
 *
 * 触发时机：
 * - ProcessLifecycleOwner ON_START（App 进入前台）→ 静音
 * - ProcessLifecycleOwner ON_STOP（App 退到后台）→ 静音
 *
 * 注意：只操作 [AudioManager.STREAM_MUSIC]，不碰铃声音量。
 * 用户播放视频时可以手动调大，退出 App 后自动归零。
 */
@Singleton
class VolumePrivacyHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Timber.d("VolumePrivacyHelper registered on ProcessLifecycleOwner")
    }

    override fun onStart(owner: LifecycleOwner) {
        muteMediaVolume()
    }

    override fun onStop(owner: LifecycleOwner) {
        muteMediaVolume()
    }

    fun muteMediaVolume() {
        runCatching {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                0,
                0, // no UI, no sound
            )
        }.onFailure {
            Timber.w(it, "Failed to mute media volume")
        }
    }
}
