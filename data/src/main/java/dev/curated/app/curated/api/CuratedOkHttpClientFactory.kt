package dev.curated.app.curated.api

import java.util.concurrent.TimeUnit
import okhttp3.CookieJar
import okhttp3.OkHttpClient

object CuratedOkHttpClientFactory {
    fun create(
        cookieJar: CookieJar,
        clientVersion: String,
        osVersion: String,
        requestTimeoutMillis: Long = 30_000L,
        connectTimeoutMillis: Long = 30_000L,
        socketTimeoutMillis: Long = 30_000L,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .callTimeout(requestTimeoutMillis, TimeUnit.MILLISECONDS)
            .connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(socketTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(socketTimeoutMillis, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", "CuratedAndroid/$clientVersion (Android $osVersion)")
                        .header("X-Curated-Client", "android")
                        .header("X-Curated-Client-Version", clientVersion)
                        .header("X-Curated-OS", "Android")
                        .header("X-Curated-OS-Version", osVersion)
                        .build()
                chain.proceed(request)
            }
            .build()
}
