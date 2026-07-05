package dev.curated.app.curated.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CuratedUrlResolverTest {
    @Test
    fun normalizeBaseUrlRemovesTrailingSlashAndApiPrefix() {
        assertEquals("http://192.168.1.23:8080", CuratedUrlResolver.normalizeBaseUrl("http://192.168.1.23:8080"))
        assertEquals("http://192.168.1.23:8080", CuratedUrlResolver.normalizeBaseUrl("http://192.168.1.23:8080/"))
        assertEquals("http://192.168.1.23:8080", CuratedUrlResolver.normalizeBaseUrl("http://192.168.1.23:8080/api"))
        assertEquals("http://192.168.1.23:8080", CuratedUrlResolver.normalizeBaseUrl("http://192.168.1.23:8080/api/"))
    }

    @Test
    fun normalizeBaseUrlRejectsUnsupportedSchemes() {
        assertThrows(IllegalArgumentException::class.java) {
            CuratedUrlResolver.normalizeBaseUrl("192.168.1.23:8080")
        }
        assertThrows(IllegalArgumentException::class.java) {
            CuratedUrlResolver.normalizeBaseUrl("ftp://192.168.1.23:8080")
        }
    }

    @Test
    fun apiUrlBuildsApiPathsFromNormalizedBaseUrl() {
        assertEquals(
            "http://192.168.1.23:8080/api/health",
            CuratedUrlResolver.apiUrl("http://192.168.1.23:8080/api/", "/health"),
        )
        assertEquals(
            "http://192.168.1.23:8080/api/library/movies",
            CuratedUrlResolver.apiUrl("http://192.168.1.23:8080", "library/movies"),
        )
    }

    @Test
    fun absoluteUrlResolvesRelativeApiUrlsAgainstBaseUrl() {
        assertEquals(
            "http://192.168.1.23:8080/api/library/movies/movie-1/stream",
            CuratedUrlResolver.absoluteUrl(
                "http://192.168.1.23:8080/api",
                "/api/library/movies/movie-1/stream",
            ),
        )
        assertEquals(
            "http://192.168.1.23:8080/api/library/movies/movie-1/asset/cover",
            CuratedUrlResolver.absoluteUrl(
                "http://192.168.1.23:8080",
                "api/library/movies/movie-1/asset/cover",
            ),
        )
        assertEquals(
            "https://cdn.example.test/cover.jpg",
            CuratedUrlResolver.absoluteUrl(
                "http://192.168.1.23:8080",
                "https://cdn.example.test/cover.jpg",
            ),
        )
    }

    @Test
    fun absoluteUrlKeepsMissingValuesMissing() {
        assertNull(CuratedUrlResolver.absoluteUrl("http://192.168.1.23:8080", null))
        assertNull(CuratedUrlResolver.absoluteUrl("http://192.168.1.23:8080", ""))
        assertNull(CuratedUrlResolver.absoluteUrl("http://192.168.1.23:8080", "   "))
    }
}
