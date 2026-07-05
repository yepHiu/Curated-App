package dev.curated.app.setup.presentation.addserver

import dev.curated.app.curated.api.AuthStatusDto
import dev.curated.app.models.Server
import dev.curated.app.models.ServerWithAddresses
import dev.curated.app.models.User
import dev.curated.app.settings.domain.AppPreferences
import dev.curated.app.setup.domain.SetupRepository
import dev.curated.app.setup.presentation.MainDispatcherRule
import dev.curated.app.setup.presentation.TestSharedPreferences
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.jellyfin.sdk.model.api.QuickConnectResult
import org.jellyfin.sdk.model.api.ServerDiscoveryInfo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddServerViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun connectNavigatesHomeWhenPinLockIsDisabled() = runTest {
        val appPreferences = AppPreferences(TestSharedPreferences())
        val viewModel =
            AddServerViewModel(
                repository = FakeSetupRepository(authStatus = authStatus(pinEnabled = false)),
                appPreferences = appPreferences,
            )

        viewModel.onAction(AddServerAction.OnConnectClick("http://server"))

        assertEquals(AddServerEvent.NavigateHome, viewModel.events.first())
        assertEquals("server-id", appPreferences.getValue(appPreferences.currentServer))
    }

    @Test
    fun connectNavigatesAuthLockWhenPinLockIsEnabledAndLocked() = runTest {
        val viewModel =
            AddServerViewModel(
                repository = FakeSetupRepository(authStatus = authStatus(pinEnabled = true)),
                appPreferences = AppPreferences(TestSharedPreferences()),
            )

        viewModel.onAction(AddServerAction.OnConnectClick("http://server"))

        assertEquals(AddServerEvent.NavigateAuthLock, viewModel.events.first())
    }

    private fun authStatus(pinEnabled: Boolean): AuthStatusDto =
        AuthStatusDto(
            pinEnabled = pinEnabled,
            unlocked = false,
            setupRequired = false,
            pinLength = 4,
            trustedForever = false,
            sessionTtlMinutes = 60,
            lanRequiresPin = false,
            lockOnRestart = false,
        )

    private class FakeSetupRepository(private val authStatus: AuthStatusDto) : SetupRepository {
        override fun discoverServers(): Flow<ServerDiscoveryInfo> = emptyFlow()

        override suspend fun getServers(): List<ServerWithAddresses> = emptyList()

        override suspend fun getCurrentServer(): Server? = null

        override suspend fun deleteServer(serverId: String) = Unit

        override suspend fun getIsQuickConnectEnabled(): Boolean = false

        override suspend fun initiateQuickConnect(): QuickConnectResult = TODO()

        override suspend fun getQuickConnectState(secret: String): QuickConnectResult = TODO()

        override suspend fun setCurrentServer(serverId: String) = Unit

        override suspend fun addServer(address: String): Server =
            Server(
                id = "server-id",
                name = "Curated",
                currentServerAddressId = UUID.randomUUID(),
                currentUserId = null,
            )

        override suspend fun getCuratedAuthStatus(): AuthStatusDto = authStatus

        override suspend fun unlockCurated(
            pin: String,
            trustedForever: Boolean,
        ): AuthStatusDto = TODO()

        override suspend fun loadDisclaimer(): String? = null

        override suspend fun login(username: String, password: String) = Unit

        override suspend fun loginWithSecret(secret: String) = Unit

        override suspend fun getUsers(serverId: String): List<User> = emptyList()

        override suspend fun getPublicUsers(serverId: String): List<User> = emptyList()

        override suspend fun getCurrentUser(): User? = null

        override suspend fun deleteUser(userId: UUID) = Unit

        override suspend fun setCurrentUser(userId: UUID) = Unit

        override suspend fun setCurrentAddress(addressId: UUID) = Unit
    }
}
