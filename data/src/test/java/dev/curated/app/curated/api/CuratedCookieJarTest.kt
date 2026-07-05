package dev.curated.app.curated.api

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedCookieJarTest {
    @Test
    fun returnsSavedCuratedAuthCookieForMatchingRequestUrl() {
        val jar = CuratedCookieJar()
        val url = "http://192.168.1.23:8080/api/auth/unlock".toHttpUrl()
        val cookie =
            Cookie.Builder()
                .name("curated_auth")
                .value("session")
                .domain("192.168.1.23")
                .path("/")
                .build()

        jar.saveFromResponse(url, listOf(cookie))

        assertEquals(listOf(cookie), jar.loadForRequest("http://192.168.1.23:8080/api/library/movies".toHttpUrl()))
    }

    @Test
    fun doesNotReturnCookiesForDifferentHosts() {
        val jar = CuratedCookieJar()
        val cookie =
            Cookie.Builder()
                .name("curated_auth")
                .value("session")
                .domain("192.168.1.23")
                .path("/")
                .build()

        jar.saveFromResponse("http://192.168.1.23:8080/api/auth/unlock".toHttpUrl(), listOf(cookie))

        assertTrue(jar.loadForRequest("http://192.168.1.24:8080/api/library/movies".toHttpUrl()).isEmpty())
    }

    @Test
    fun clearRemovesSavedCookies() {
        val jar = CuratedCookieJar()
        val url = "http://192.168.1.23:8080/api/auth/unlock".toHttpUrl()
        val cookie =
            Cookie.Builder()
                .name("curated_auth")
                .value("session")
                .domain("192.168.1.23")
                .path("/")
                .build()

        jar.saveFromResponse(url, listOf(cookie))
        jar.clear()

        assertTrue(jar.loadForRequest(url).isEmpty())
    }
}
