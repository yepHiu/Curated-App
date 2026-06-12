package dev.jdtech.jellyfin.curated.api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CuratedCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(this.cookies) {
            cookies.forEach { newCookie ->
                this.cookies.removeAll { existingCookie ->
                    existingCookie.name == newCookie.name &&
                        existingCookie.domain == newCookie.domain &&
                        existingCookie.path == newCookie.path
                }
                this.cookies.add(newCookie)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        synchronized(cookies) { cookies.filter { it.matches(url) } }

    fun clear() {
        synchronized(cookies) { cookies.clear() }
    }
}
