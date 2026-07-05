package dev.curated.app.presentation.privacy

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import timber.log.Timber

/** 三连击允许的最大间隔（毫秒） */
private const val TAP_TIMEOUT_MS = 1000L

/**
 * 视线防护遮罩层。
 *
 * 使用 [ProcessLifecycleOwner] 监听 App 前后台切换：
 * 每次 App 从后台回到前台时显示全屏暗色遮罩，
 * 用户需在 1 秒内连续点击 3 次才能解锁。
 * 内部 Activity 跳转（如进入/返回播放页）不会重新激活遮罩。
 *
 * API 31+ 时底层内容同步施加 20dp 模糊。
 */
@Composable
fun GazeProtectionOverlay(
    content: @Composable () -> Unit,
) {
    var isActive by remember { mutableStateOf(true) }
    var tapCount by remember { mutableIntStateOf(0) }
    var firstTapTimeMs by remember { mutableLongStateOf(0L) }

    // 监听 ProcessLifecycleOwner：App 回到前台 → 重新激活遮罩
    DisposableEffect(Unit) {
        val observer =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    isActive = true
                    tapCount = 0
                    firstTapTimeMs = 0L
                    Timber.d("GazeProtectionOverlay activated on process ON_START")
                }
            }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        onDispose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 底层：App 实际内容（遮罩激活 + API 31+ 时模糊）
        Box(
            modifier =
                if (isActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(radius = 20.dp)
                } else {
                    Modifier
                },
        ) {
            content()
        }

        // 顶层：暗色遮罩 + 三连击解锁
        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            val now = System.currentTimeMillis()
                            if (tapCount == 0 || now - firstTapTimeMs > TAP_TIMEOUT_MS) {
                                tapCount = 1
                                firstTapTimeMs = now
                            } else {
                                tapCount++
                            }

                            Timber.d("GazeProtection tap: count=$tapCount, elapsed=${now - firstTapTimeMs}ms")

                            if (tapCount >= 3) {
                                isActive = false
                                tapCount = 0
                                firstTapTimeMs = 0L
                                Timber.d("GazeProtectionOverlay dismissed")
                            }
                        },
            )
        }
    }
}
