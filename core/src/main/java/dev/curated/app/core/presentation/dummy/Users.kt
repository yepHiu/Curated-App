package dev.curated.app.core.presentation.dummy

import dev.curated.app.models.User
import java.util.UUID

val dummyUser = User(id = UUID.randomUUID(), name = "Username", serverId = "")

val dummyUsers = listOf(dummyUser)
