package dev.curated.app.core.icon

import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherIconAlignmentTest {
    @Test
    fun launcherForegroundSymbolIsVisuallyCentered() {
        val imageFile = findLauncherForeground()
        val image = ImageIO.read(imageFile)

        var weightedX = 0.0
        var weightedY = 0.0
        var totalWeight = 0.0

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                val alpha = argb ushr 24 and 0xff
                val red = argb ushr 16 and 0xff
                val green = argb ushr 8 and 0xff
                val blue = argb and 0xff
                val pinkWeight = red - maxOf(green, blue)

                if (
                    alpha > 8 &&
                        red > 180 &&
                        green > 40 &&
                        blue > 80 &&
                        pinkWeight > 40
                ) {
                    weightedX += x * pinkWeight
                    weightedY += y * pinkWeight
                    totalWeight += pinkWeight
                }
            }
        }

        val canvasCenterX = (image.width - 1) / 2.0
        val canvasCenterY = (image.height - 1) / 2.0
        assertEquals(canvasCenterX, weightedX / totalWeight, 3.0)
        assertEquals(canvasCenterY, weightedY / totalWeight, 3.0)
    }

    private fun findLauncherForeground(): File {
        val candidates =
            listOf(
                File("src/main/res/drawable/ic_launcher_foreground.png"),
                File("core/src/main/res/drawable/ic_launcher_foreground.png"),
            )
        return candidates.first { it.exists() }
    }
}
