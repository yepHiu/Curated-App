package dev.jdtech.jellyfin

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerOrientationPolicyTest {
    @Test
    fun playerActivitiesStartInSensorOrientation() {
        val manifest = projectFile("src/main/AndroidManifest.xml")
        val document =
            DocumentBuilderFactory.newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(manifest)

        assertEquals("sensor", activityOrientation(document, ".PlayerActivity"))
        assertEquals("sensor", activityOrientation(document, ".CuratedPlayerActivity"))
    }

    @Test
    fun playerUnlockRestoresSensorOrientationInsteadOfLandscapeOnlyOrientation() {
        val activitySources =
            listOf(
                "src/main/java/dev/jdtech/jellyfin/PlayerActivity.kt",
                "src/main/java/dev/jdtech/jellyfin/CuratedPlayerActivity.kt",
            )

        activitySources.forEach { path ->
            val source = projectFile(path).readText()

            assertFalse(
                "$path should not force landscape-only playback after unlocking controls",
                source.contains("SCREEN_ORIENTATION_SENSOR_LANDSCAPE"),
            )
            assertTrue(
                "$path should restore sensor orientation after unlocking controls",
                source.contains("requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR"),
            )
        }
    }

    private fun activityOrientation(
        document: org.w3c.dom.Document,
        activityName: String,
    ): String {
        val activities = document.getElementsByTagName("activity")
        for (index in 0 until activities.length) {
            val activity = activities.item(index)
            val name = activity.attributes.getNamedItemNS(ANDROID_NS, "name")?.nodeValue
            if (name == activityName) {
                return activity.attributes.getNamedItemNS(ANDROID_NS, "screenOrientation")
                    ?.nodeValue
                    ?: ""
            }
        }
        error("Activity $activityName was not found in AndroidManifest.xml")
    }

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error(
                "Could not find $relativePath from ${File(".").absoluteFile.normalize().path}"
            )
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
