package dev.jdtech.jellyfin.curated.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedErrorMapperTest {
    @Test
    fun mapsLockedAppErrorToAuthLockedFailure() {
        val failure =
            CuratedErrorMapper.map(
                statusCode = 423,
                body = """{"code":"AUTH_LOCKED","message":"locked","retryable":false}""",
            )

        assertEquals(CuratedFailure.AuthLocked, failure.kind)
        assertEquals("AUTH_LOCKED", failure.code)
        assertEquals("locked", failure.message)
        assertFalse(failure.retryable)
    }

    @Test
    fun mapsInvalidPinAppErrorToInvalidPinFailure() {
        val failure =
            CuratedErrorMapper.map(
                statusCode = 401,
                body = """{"code":"AUTH_INVALID_PIN","message":"invalid pin","retryable":false}""",
            )

        assertEquals(CuratedFailure.InvalidPin, failure.kind)
        assertEquals("AUTH_INVALID_PIN", failure.code)
        assertEquals("invalid pin", failure.message)
    }

    @Test
    fun mapsNotFoundAndConflictByStatusCode() {
        val notFound =
            CuratedErrorMapper.map(
                statusCode = 404,
                body = """{"code":"COMMON_NOT_FOUND","message":"missing","retryable":false}""",
            )
        val conflict =
            CuratedErrorMapper.map(
                statusCode = 409,
                body = """{"code":"COMMON_CONFLICT","message":"conflict","retryable":false}""",
            )

        assertEquals(CuratedFailure.NotFound, notFound.kind)
        assertEquals(CuratedFailure.Conflict, conflict.kind)
    }

    @Test
    fun mapsServerErrorsAsRetryableWhenBodyIsMissing() {
        val failure = CuratedErrorMapper.map(statusCode = 503, body = null)

        assertEquals(CuratedFailure.Server, failure.kind)
        assertEquals("HTTP_503", failure.code)
        assertTrue(failure.retryable)
    }

    @Test
    fun mapsInvalidErrorBodiesToUnknownFailure() {
        val failure = CuratedErrorMapper.map(statusCode = 418, body = "not-json")

        assertEquals(CuratedFailure.Unknown, failure.kind)
        assertEquals("HTTP_418", failure.code)
        assertEquals("HTTP 418", failure.message)
    }
}
