package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedPageHeaderPolicyTest {
    @Test
    fun topLevelHeaderKeepsOneSharedHeightAndSafeTopPadding() {
        assertEquals(8.dp, curatedPageHeaderTopPadding(0.dp))
        assertEquals(32.dp, curatedPageHeaderTopPadding(24.dp))
        assertEquals(56.dp, curatedPageHeaderContentHeight())
        assertEquals(8.dp, curatedPageHeaderBottomPadding())
    }

    @Test
    fun brandWordmarkUsesCuratedNameAndOutfitFont() {
        assertEquals("Curated", curatedPageHeaderBrandWordmarkText())
        assertEquals("Outfit", curatedPageHeaderBrandFontName())
        assertTrue(projectFile("src/main/res/font/outfit_bold.ttf").exists())
    }

    @Test
    fun brandWordmarkIsTextOnlyWithoutDecorativeIcon() {
        val source =
            projectFile("src/main/java/dev/curated/app/presentation/curated/CuratedPageHeader.kt")
                .readText()
                .replace("\r\n", "\n")
        val brandBlock =
            source
                .substringAfter("internal fun CuratedBrandWordmark")
                .substringBefore("@Composable\ninternal fun CuratedPageHeaderTitle")

        assertFalse(brandBlock.contains("Icon("))
        assertFalse(brandBlock.contains("ic_sparkles"))
    }

    @Test
    fun connectionStatusUsesSoftTopChromeLanguage() {
        val labels =
            listOf(
                curatedPageHeaderStatusLabel(CuratedPageHeaderStatus.Connecting),
                curatedPageHeaderStatusLabel(CuratedPageHeaderStatus.Reconnecting),
            )

        assertEquals(listOf("Connecting", "Reconnecting"), labels)
        labels.forEach { label ->
            assertFalse(label.contains("error", ignoreCase = true))
            assertFalse(label.contains("failed", ignoreCase = true))
        }
    }

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
    }
}
