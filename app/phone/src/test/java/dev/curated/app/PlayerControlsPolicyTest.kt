package dev.curated.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class PlayerControlsPolicyTest {
    @Test
    fun playerControlsDoNotExposeAudioOrSubtitleTrackButtons() {
        val layout = projectFile("src/main/res/layout/exo_main_controls.xml")
        val document =
            DocumentBuilderFactory.newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(layout)
        val ids = document.allAndroidIds()

        assertFalse(ids.contains("@+id/btn_audio_track"))
        assertFalse(ids.contains("@+id/btn_subtitle"))
    }

    @Test
    fun playerActivitiesDoNotWireAudioOrSubtitleTrackControls() {
        val forbiddenPatterns =
            listOf(
                "R.id.btn_audio_track",
                "R.id.btn_subtitle",
                "TrackSelectionDialogFragment",
                "showTrackDialog",
            )

        listOf(
                "src/main/java/dev/curated/app/PlayerActivity.kt",
                "src/main/java/dev/curated/app/CuratedPlayerActivity.kt",
            )
            .forEach { path ->
                val source = projectFile(path).readText()
                forbiddenPatterns.forEach { pattern ->
                    assertFalse("$path should not contain $pattern", source.contains(pattern))
                }
            }
    }

    @Test
    fun playerCenterControlsUseCompactSymmetricPortraitLayout() {
        val document = parseMainControlsLayout()
        val centerButtonIds = listOf("exo_prev", "exo_rew", "exo_play_pause", "exo_ffwd", "exo_next")
        val buttons = centerButtonIds.associateWith { document.findElementByAndroidId(it) }
        val centerControls = buttons.getValue("exo_play_pause").parentNode as Element

        centerButtonIds.forEach { buttonId ->
            assertEquals(
                "$buttonId should stay in the centered controls row",
                centerControls,
                buttons.getValue(buttonId).parentNode,
            )
        }

        assertEquals("match_parent", centerControls.androidAttr("layout_width"))
        assertEquals("center", centerControls.androidAttr("gravity"))
        assertEquals(COMPACT_CENTER_CONTROL_SPACING, buttons.getValue("exo_prev").androidAttr("layout_marginEnd"))
        assertEquals(COMPACT_CENTER_CONTROL_SPACING, buttons.getValue("exo_rew").androidAttr("layout_marginEnd"))
        assertEquals(COMPACT_CENTER_CONTROL_SPACING, buttons.getValue("exo_ffwd").androidAttr("layout_marginStart"))
        assertEquals(COMPACT_CENTER_CONTROL_SPACING, buttons.getValue("exo_next").androidAttr("layout_marginStart"))
    }

    private fun org.w3c.dom.Document.allAndroidIds(): Set<String> {
        val ids = mutableSetOf<String>()
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val id = nodes.item(index).attributes.getNamedItemNS(ANDROID_NS, "id")?.nodeValue
            if (id != null) {
                ids += id
            }
        }
        return ids
    }

    private fun parseMainControlsLayout() =
        DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(projectFile("src/main/res/layout/exo_main_controls.xml"))

    private fun org.w3c.dom.Document.findElementByAndroidId(id: String): Element {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val node = nodes.item(index) as? Element ?: continue
            if (node.androidAttr("id") == "@+id/$id") {
                return node
            }
        }
        assertNotNull("Expected to find @+id/$id", null)
        error("Expected to find @+id/$id")
    }

    private fun Element.androidAttr(name: String): String? =
        attributes.getNamedItemNS(ANDROID_NS, name)?.nodeValue

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error(
                "Could not find $relativePath from ${File(".").absoluteFile.normalize().path}"
            )
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        const val COMPACT_CENTER_CONTROL_SPACING = "8dp"
    }
}
