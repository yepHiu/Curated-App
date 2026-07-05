package dev.curated.app.setup.presentation.addserver

import dev.curated.app.models.DiscoveredServer
import dev.curated.app.models.UiText

data class AddServerState(
    val isLoading: Boolean = false,
    val serverAddress: String = CuratedMvpDefaults.defaultBackendUrl,
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val error: Collection<UiText>? = null,
)

object CuratedMvpDefaults {
    const val defaultBackendUrl: String = "http://192.168.31.251:8081/"
}
