package dev.curated.app.core.privacy

private const val HALF_OPACITY_ALPHA = 128
private const val NEUTRAL_GRAY_CHANNEL = 128

object PrivacyOverlayStyle {
    fun grayBlurScrimColor(): Int =
        argb(
            alpha = HALF_OPACITY_ALPHA,
            red = NEUTRAL_GRAY_CHANNEL,
            green = NEUTRAL_GRAY_CHANNEL,
            blue = NEUTRAL_GRAY_CHANNEL,
        )

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
