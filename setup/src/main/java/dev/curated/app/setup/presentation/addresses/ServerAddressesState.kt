package dev.curated.app.setup.presentation.addresses

import dev.curated.app.models.ServerAddress

data class ServerAddressesState(val addresses: List<ServerAddress> = emptyList())
