package dev.jdtech.jellyfin

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class CuratedNavigationLabelsTest {
    @Test
    fun simplifiedChineseMediaTabUsesMovieLibraryLabel() {
        val strings = parseStrings("core/src/main/res/values-zh-rCN/strings.xml")

        assertEquals("影片库", strings.getValue("title_media"))
    }

    private fun parseStrings(relativePath: String): Map<String, String> {
        val document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(projectFile(relativePath))
        val strings = linkedMapOf<String, String>()
        val nodes = document.getElementsByTagName("string")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            val name = element.getAttribute("name")
            if (name.isNotBlank()) {
                strings[name] = element.textContent
            }
        }
        return strings
    }

    private fun projectFile(relativePath: String): File =
        listOf(File(relativePath), File("../..", relativePath))
            .firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
}
