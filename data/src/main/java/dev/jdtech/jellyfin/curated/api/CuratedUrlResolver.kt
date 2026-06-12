package dev.jdtech.jellyfin.curated.api

object CuratedUrlResolver {
    fun normalizeBaseUrl(input: String): String {
        var url = input.trim()
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "Curated baseUrl must start with http:// or https://"
        }

        while (url.endsWith("/")) {
            url = url.dropLast(1)
        }

        if (url.endsWith("/api")) {
            url = url.removeSuffix("/api")
        }

        return url
    }

    fun apiUrl(baseUrl: String, path: String): String {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val normalizedPath = path.trim().trimStart('/')
        return if (normalizedPath.startsWith("api/")) {
            "$normalizedBaseUrl/$normalizedPath"
        } else {
            "$normalizedBaseUrl/api/$normalizedPath"
        }
    }

    fun absoluteUrl(baseUrl: String, value: String?): String? {
        val path = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }

        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val normalizedPath = path.trimStart('/')
        return "$normalizedBaseUrl/$normalizedPath"
    }
}
