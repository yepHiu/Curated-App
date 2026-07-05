package dev.curated.app.presentation.privacy

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import dev.curated.app.core.privacy.ActivityPrivacyLifecycleTracker
import dev.curated.app.core.privacy.PrivacyOverlayGate
import java.util.Collections
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

private const val PRIVACY_OVERLAY_TAG = "curated_privacy_overlay"
private const val BLUR_RADIUS_DP = 20f

@Singleton
class GazeProtectionCoordinator @Inject constructor() : Application.ActivityLifecycleCallbacks {
    private val activeActivities =
        Collections.newSetFromMap(WeakHashMap<Activity, Boolean>())
    private val gates = WeakHashMap<Activity, PrivacyOverlayGate>()
    private val lifecycleTracker =
        ActivityPrivacyLifecycleTracker(
            onEnterForeground = { showOverlayOnActiveActivities() },
            onExitForeground = { showOverlayOnActiveActivities() },
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
        installOverlay(activity)
        lifecycleTracker.onActivityStarted()
    }

    override fun onActivityStopped(activity: Activity) {
        lifecycleTracker.onActivityStopped()
        activeActivities.remove(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        activeActivities.remove(activity)
        gates.remove(activity)
    }

    private fun showOverlayOnActiveActivities() {
        activeActivities.forEach { activity ->
            installOverlay(activity)?.let { overlay ->
                val gate = gates.getOrPut(activity) { PrivacyOverlayGate() }
                gate.activate()
                overlay.visibility = View.VISIBLE
                applyContentBlur(activity, enabled = true)
                overlay.bringToFront()
            }
        }
    }

    private fun hideOverlay(activity: Activity) {
        gates[activity]?.dismiss()
        findOverlay(activity)?.visibility = View.GONE
        applyContentBlur(activity, enabled = false)
    }

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
                setBackgroundColor(Color.argb(128, 0, 0, 0))
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
