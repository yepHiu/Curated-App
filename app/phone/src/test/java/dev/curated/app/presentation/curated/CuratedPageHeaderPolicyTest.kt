package dev.curated.app.presentation.curated

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun brandWordmarkUsesRequestedCreativeLetters() {
        assertEquals("creative", curatedPageHeaderBrandWordmarkText())
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
}
