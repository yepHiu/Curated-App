package dev.curated.app.core.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PrivacyOverlayStyleTest {
    @Test
    fun blurScrimUsesHalfOpacityGrayOverlay() {
        val color = PrivacyOverlayStyle.grayBlurScrimColor()

        assertEquals(128, color.alpha)
        assertNotEquals(argb(alpha = 128, red = 0, green = 0, blue = 0), color)
        assertEquals(argb(alpha = 128, red = 128, green = 128, blue = 128), color)
    }

    private val Int.alpha: Int
        get() = ushr(24) and 0xff

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
