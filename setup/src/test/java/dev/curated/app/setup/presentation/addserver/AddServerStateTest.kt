package dev.curated.app.setup.presentation.addserver

import org.junit.Assert.assertEquals
import org.junit.Test

class AddServerStateTest {
    @Test
    fun defaultsToTemporaryCuratedBackendAddress() {
        assertEquals("http://192.168.31.251:8081/", AddServerState().serverAddress)
    }
}
