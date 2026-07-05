package dev.curated.app.core.privacy

import kotlin.math.roundToInt

private const val HALF_OPACITY_ALPHA = 128
private const val COLOR_MIX_PRIMARY_WEIGHT = 0.20f
private const val DARK_BACKGROUND_RED = 13
private const val DARK_BACKGROUND_GREEN = 15
private const val DARK_BACKGROUND_BLUE = 26
private const val BRAND_PRIMARY_RED = 254
private const val BRAND_PRIMARY_GREEN = 98
private const val BRAND_PRIMARY_BLUE = 142

object PrivacyOverlayStyle {
    fun mixedBlurScrimColor(): Int =
        argb(
            alpha = HALF_OPACITY_ALPHA,
            red = mixChannel(background = DARK_BACKGROUND_RED, foreground = BRAND_PRIMARY_RED),
            green = mixChannel(
                background = DARK_BACKGROUND_GREEN,
                foreground = BRAND_PRIMARY_GREEN,
            ),
            blue = mixChannel(background = DARK_BACKGROUND_BLUE, foreground = BRAND_PRIMARY_BLUE),
        )

    private fun mixChannel(background: Int, foreground: Int): Int =
        (background + (foreground - background) * COLOR_MIX_PRIMARY_WEIGHT)
            .roundToInt()

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
