package dev.curated.app.curated.api

import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class CuratedOkHttpClientFactoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun createUsesTheProvidedCookieJar() {
        val cookieJar = CuratedCookieJar()

        val client =
            CuratedOkHttpClientFactory.create(
                cookieJar = cookieJar,
                clientVersion = "0.1.0",
                osVersion = "15",
            )

        assertSame(cookieJar, client.cookieJar)
    }

    @Test
    fun createAddsCuratedClientHeaders() {
        server.enqueue(MockResponse().setResponseCode(204))
        val client =
            CuratedOkHttpClientFactory.create(
                cookieJar = CuratedCookieJar(),
                clientVersion = "0.1.0",
                osVersion = "15",
            )

        client
            .newCall(Request.Builder().url(server.url("/api/health")).build())
            .execute()
            .close()

        val request = server.takeRequest()
        assertEquals("android", request.getHeader("X-Curated-Client"))
        assertEquals("0.1.0", request.getHeader("X-Curated-Client-Version"))
        assertEquals("Android", request.getHeader("X-Curated-OS"))
        assertEquals("15", request.getHeader("X-Curated-OS-Version"))
        assertEquals("CuratedAndroid/0.1.0 (Android 15)", request.getHeader("User-Agent"))
    }
}
