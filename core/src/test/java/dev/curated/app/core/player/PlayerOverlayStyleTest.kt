package dev.curated.app.core.player

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerOverlayStyleTest {
    @Test
    fun playerControlOverlayUsesFortyPercentBlack() {
        val color = readColorResource(name = "player_background")

        assertEquals("#66000000", color)
    }

    private fun readColorResource(name: String): String {
        val colorsFile = findColorsXml()
        val document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(colorsFile)
        val colorNodes = document.getElementsByTagName("color")

        for (index in 0 until colorNodes.length) {
            val node = colorNodes.item(index)
            if (node.attributes.getNamedItem("name")?.nodeValue == name) {
                return node.textContent.trim()
            }
        }

        error("Color resource not found: $name")
    }

    private fun findColorsXml(): File {
        val candidates =
            listOf(
                File("src/main/res/values/colors.xml"),
                File("core/src/main/res/values/colors.xml"),
            )
        return candidates.first { it.exists() }
    }
}
