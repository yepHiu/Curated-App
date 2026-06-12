package dev.jdtech.jellyfin.presentation.curated

import dev.jdtech.jellyfin.core.R as CoreR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CuratedHistoryFormatterTest {
    @Test
    fun progressFractionUsesDurationAndCoercesIntoRange() {
        assertEquals(0.25f, curatedHistoryProgressFraction(positionSec = 30.0, durationSec = 120.0))
        assertEquals(1.0f, curatedHistoryProgressFraction(positionSec = 150.0, durationSec = 120.0))
        assertEquals(0.0f, curatedHistoryProgressFraction(positionSec = -5.0, durationSec = 120.0))
        assertEquals(0.0f, curatedHistoryProgressFraction(positionSec = 30.0, durationSec = null))
        assertEquals(0.0f, curatedHistoryProgressFraction(positionSec = 30.0, durationSec = 0.0))
    }

    @Test
    fun progressPercentTextRoundsCoercedFraction() {
        assertEquals("25%", curatedHistoryProgressPercentText(positionSec = 30.0, durationSec = 120.0))
        assertEquals("100%", curatedHistoryProgressPercentText(positionSec = 150.0, durationSec = 120.0))
        assertEquals("0%", curatedHistoryProgressPercentText(positionSec = 30.0, durationSec = null))
    }

    @Test
    fun progressTimeTextFormatsPositionAndDuration() {
        assertEquals("1:05 / 1:02:03", curatedHistoryProgressTimeText(positionSec = 65.0, durationSec = 3723.0))
        assertEquals("0:00 / 2:00", curatedHistoryProgressTimeText(positionSec = -2.0, durationSec = 120.0))
        assertEquals("1:05 watched", curatedHistoryProgressTimeText(positionSec = 65.0, durationSec = null))
    }

    @Test
    fun imageUrlPrefersThumbOverCover() {
        assertEquals("thumb.jpg", curatedHistoryImageUrl(thumbUrl = "thumb.jpg", coverUrl = "cover.jpg"))
        assertEquals("cover.jpg", curatedHistoryImageUrl(thumbUrl = null, coverUrl = "cover.jpg"))
        assertNull(curatedHistoryImageUrl(thumbUrl = null, coverUrl = null))
    }

    @Test
    fun headerSubtitleDoesNotShowRecordCount() {
        assertEquals(CoreR.string.history_recent_activity, curatedHistoryHeaderSubtitleResId(itemCount = 12))
    }
}
