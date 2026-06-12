package dev.jdtech.jellyfin.curated.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class CuratedFailure {
    AuthLocked,
    InvalidPin,
    NotFound,
    Conflict,
    Server,
    Unknown,
}

data class CuratedFailureResult(
    val kind: CuratedFailure,
    val statusCode: Int,
    val code: String,
    val message: String,
    val retryable: Boolean,
)

class CuratedApiException(val failure: CuratedFailureResult) : RuntimeException(failure.message)

object CuratedErrorMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun map(statusCode: Int, body: String?): CuratedFailureResult {
        val appError = body?.takeIf { it.isNotBlank() }?.let { parseAppError(it) }
        val code = appError?.code ?: "HTTP_$statusCode"
        val message = appError?.message ?: "HTTP $statusCode"
        val retryable = appError?.retryable ?: (statusCode >= 500)

        return CuratedFailureResult(
            kind = kindFor(statusCode = statusCode, code = code),
            statusCode = statusCode,
            code = code,
            message = message,
            retryable = retryable,
        )
    }

    private fun parseAppError(body: String): AppError? =
        try {
            json.decodeFromString<AppError>(body)
        } catch (_: Exception) {
            null
        }

    private fun kindFor(statusCode: Int, code: String): CuratedFailure =
        when {
            code == "AUTH_LOCKED" -> CuratedFailure.AuthLocked
            code == "AUTH_INVALID_PIN" -> CuratedFailure.InvalidPin
            code == "COMMON_NOT_FOUND" || statusCode == 404 -> CuratedFailure.NotFound
            code == "COMMON_CONFLICT" || statusCode == 409 -> CuratedFailure.Conflict
            statusCode >= 500 -> CuratedFailure.Server
            else -> CuratedFailure.Unknown
        }
}

@Serializable
private data class AppError(
    val code: String,
    val message: String,
    val retryable: Boolean = false,
)
