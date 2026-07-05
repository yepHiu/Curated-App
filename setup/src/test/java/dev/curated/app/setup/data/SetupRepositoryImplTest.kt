package dev.curated.app.setup.data

import dev.curated.app.models.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupRepositoryImplTest {
    @Test
    fun curatedServerIdNormalizesBaseUrlBeforeHashing() {
        assertEquals(
            curatedServerIdForBaseUrl("http://192.168.1.23:8080"),
            curatedServerIdForBaseUrl("http://192.168.1.23:8080/api/"),
        )
    }

    @Test
    fun curatedServerIdIsNavigationSafeUuidString() {
        val serverId = curatedServerIdForBaseUrl("http://192.168.1.23:8080/api/")

        assertTrue(
            serverId.matches(
                Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
            )
        )
    }

    @Test
    fun curatedConnectionFailureMessageIncludesHealthUrlAndCause() {
        val uiText =
            curatedConnectionFailureUiText(
                normalizedBaseUrl = "http://192.168.31.251:8081",
                cause = IllegalStateException("connection refused"),
            )

        assertTrue(uiText is UiText.DynamicString)
        val message = (uiText as UiText.DynamicString).value
        assertTrue(message.contains("http://192.168.31.251:8081/api/health"))
        assertTrue(message.contains("connection refused"))
    }
}
