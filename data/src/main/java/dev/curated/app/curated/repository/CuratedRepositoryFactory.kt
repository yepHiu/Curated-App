package dev.curated.app.curated.repository

import dev.curated.app.database.ServerDatabaseDao
import dev.curated.app.settings.domain.AppPreferences
import okhttp3.OkHttpClient

class CuratedRepositoryFactory(
    private val appPreferences: AppPreferences,
    private val database: ServerDatabaseDao,
    private val client: OkHttpClient,
) {
    fun createForCurrentServer(): CuratedRepository {
        val baseUrl = currentServerBaseUrl() ?: error("No Curated server is configured")
        return CuratedRepositoryImpl(baseUrl = baseUrl, client = client)
    }

    fun currentServerBaseUrl(): String? {
        val serverId = appPreferences.getValue(appPreferences.currentServer) ?: return null
        return database.getServerCurrentAddress(serverId)?.address
    }
}
