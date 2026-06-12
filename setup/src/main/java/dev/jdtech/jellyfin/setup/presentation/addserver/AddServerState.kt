package dev.jdtech.jellyfin.setup.presentation.addserver

import dev.jdtech.jellyfin.models.DiscoveredServer
import dev.jdtech.jellyfin.models.UiText

data class AddServerState(
    val isLoading: Boolean = false,
    val serverAddress: String = CuratedMvpDefaults.defaultBackendUrl,
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val error: Collection<UiText>? = null,
)

object CuratedMvpDefaults {
    const val defaultBackendUrl: String = "http://192.168.31.251:8081/"
}
