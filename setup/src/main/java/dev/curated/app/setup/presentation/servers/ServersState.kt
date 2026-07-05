package dev.curated.app.setup.presentation.servers

import dev.curated.app.models.ServerWithAddresses

data class ServersState(val servers: List<ServerWithAddresses> = emptyList())
