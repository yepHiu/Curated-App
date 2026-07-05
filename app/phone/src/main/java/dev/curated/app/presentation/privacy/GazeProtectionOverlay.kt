package dev.curated.app.presentation.privacy

import android.app.Activity
import android.app.Application
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import dev.curated.app.core.privacy.ActivityPrivacyLifecycleTracker
import dev.curated.app.core.privacy.PrivacyPauseOverlayTracker
import dev.curated.app.core.privacy.PrivacyOverlayGate
import dev.curated.app.core.privacy.PrivacyOverlayStyle
import dev.curated.app.settings.domain.AppPreferences
import java.util.Collections
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

private const val PRIVACY_OVERLAY_TAG = "curated_privacy_overlay"
private const val BLUR_RADIUS_DP = 20f

@Singleton
class GazeProtectionCoordinator @Inject constructor(
    private val appPreferences: AppPreferences,
) : Application.ActivityLifecycleCallbacks {
    private val activeActivities =
        Collections.newSetFromMap(WeakHashMap<Activity, Boolean>())
    private val gates = WeakHashMap<Activity, PrivacyOverlayGate>()
    private val pauseOverlayTracker = PrivacyPauseOverlayTracker()
    private val lifecycleTracker =
        ActivityPrivacyLifecycleTracker(
            onEnterForeground = { showOverlayOnActiveActivities() },
            onExitForeground = {
                pauseOverlayTracker.onAppExitedForeground()
                showOverlayOnActiveActivities()
            },
        )
    private var registered = false

    fun start(application: Application) {
        if (registered) return

        registered = true
        application.registerActivityLifecycleCallbacks(this)
        Timber.d("GazeProtectionCoordinator registered on ActivityLifecycleCallbacks")
    }

    override fun onActivityStarted(activity: Activity) {
        activeActivities.add(activity)
        applySecureScreen(activity)
        if (isGazeProtectionEnabled()) {
            installOverlay(activity)
        } else {
            hideOverlay(activity)
        }
        lifecycleTracker.onActivityStarted()
    }

    override fun onActivityStopped(activity: Activity) {
        lifecycleTracker.onActivityStopped()
        activeActivities.remove(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        activeActivities.remove(activity)
        gates.remove(activity)
        pauseOverlayTracker.clear(activity)
    }

    private fun showOverlayOnActiveActivities() {
        if (!isGazeProtectionEnabled()) {
            activeActivities.forEach(::hideOverlay)
            return
        }

        activeActivities.forEach { activity ->
            showOverlay(activity)
        }
    }

    private fun showOverlay(activity: Activity) {
        if (!isGazeProtectionEnabled()) {
            hideOverlay(activity)
            return
        }

        installOverlay(activity)?.let { overlay ->
            val gate = gates.getOrPut(activity) { PrivacyOverlayGate() }
            gate.activate()
            overlay.visibility = View.VISIBLE
            applyContentBlur(activity, enabled = true)
            overlay.bringToFront()
        }
    }

    private fun hideOverlay(activity: Activity) {
        gates[activity]?.dismiss()
        findOverlay(activity)?.visibility = View.GONE
        applyContentBlur(activity, enabled = false)
    }

    private fun applySecureScreen(activity: Activity) {
        if (appPreferences.getValue(appPreferences.privacySecureScreen)) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun isGazeProtectionEnabled(): Boolean =
        appPreferences.getValue(appPreferences.privacyGazeProtection)

    private fun installOverlay(activity: Activity): View? {
        val gate = gates.getOrPut(activity) { PrivacyOverlayGate() }
        findOverlay(activity)?.let { return it }

        val contentRoot = activity.findViewById<FrameLayout>(android.R.id.content) ?: return null
        val overlay =
            FrameLayout(activity).apply {
                tag = PRIVACY_OVERLAY_TAG
                visibility = if (gate.isActive) View.VISIBLE else View.GONE
                isClickable = true
                isFocusable = true
                setBackgroundColor(PrivacyOverlayStyle.blackBlurScrimColor())
                setOnClickListener {
                    if (gate.onTap(System.currentTimeMillis())) {
                        hideOverlay(activity)
                    }
                }
            }

        contentRoot.addView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        return overlay
    }

    private fun findOverlay(activity: Activity): View? =
        activity.findViewById<FrameLayout>(android.R.id.content)
            ?.findViewWithTag(PRIVACY_OVERLAY_TAG)

    private fun applyContentBlur(activity: Activity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val contentRoot = activity.findViewById<FrameLayout>(android.R.id.content) ?: return
        val overlay = findOverlay(activity)
        val effect =
            if (enabled) {
                val radiusPx = BLUR_RADIUS_DP * activity.resources.displayMetrics.density
                RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
            } else {
                null
            }

        for (index in 0 until contentRoot.childCount) {
            val child = contentRoot.getChildAt(index)
            if (child !== overlay) {
                child.setRenderEffect(effect)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        applySecureScreen(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        applySecureScreen(activity)
        if (!isGazeProtectionEnabled() || pauseOverlayTracker.shouldHideOverlayOnResume(activity)) {
            hideOverlay(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        applySecureScreen(activity)
        showOverlay(activity)
        if (isGazeProtectionEnabled()) {
            pauseOverlayTracker.markOverlayShownForPause(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
