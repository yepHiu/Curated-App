package dev.curated.app.setup.presentation.users

import dev.curated.app.models.User

data class UsersState(
    val users: List<User> = emptyList(),
    val publicUsers: List<User> = emptyList(),
    val serverName: String? = null,
)
