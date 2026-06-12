package dev.jdtech.jellyfin.curated.repository

import dev.jdtech.jellyfin.database.ServerDatabaseDao
import dev.jdtech.jellyfin.settings.domain.AppPreferences
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
