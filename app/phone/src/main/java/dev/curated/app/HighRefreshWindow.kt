package dev.curated.app

import android.view.Display
import android.view.Window

fun Window.applyCuratedHighRefreshRatePreference(display: Display? = decorView.display) {
    val preferredMode =
        RefreshRatePolicy.preferredMode(
            modes = display?.supportedModes?.map { it.toRefreshDisplayMode() }.orEmpty(),
            currentMode = display?.mode?.toRefreshDisplayMode(),
        ) ?: return

    attributes =
        attributes.apply {
            preferredDisplayModeId = preferredMode.id
            preferredRefreshRate = preferredMode.refreshRate
        }
}

private fun Display.Mode.toRefreshDisplayMode() =
    RefreshDisplayMode(
        id = modeId,
        physicalWidth = physicalWidth,
        physicalHeight = physicalHeight,
        refreshRate = refreshRate,
    )
