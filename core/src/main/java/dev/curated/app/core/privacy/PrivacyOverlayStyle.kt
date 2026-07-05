package dev.curated.app.core.privacy

private const val THIRTY_PERCENT_ALPHA = 77
private const val BLACK_CHANNEL = 0

object PrivacyOverlayStyle {
    fun blackBlurScrimColor(): Int =
        argb(
            alpha = THIRTY_PERCENT_ALPHA,
            red = BLACK_CHANNEL,
            green = BLACK_CHANNEL,
            blue = BLACK_CHANNEL,
        )

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
