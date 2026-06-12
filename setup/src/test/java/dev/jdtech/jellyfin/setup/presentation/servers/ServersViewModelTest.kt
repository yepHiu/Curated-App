package dev.jdtech.jellyfin.setup.presentation.servers

import dev.jdtech.jellyfin.curated.api.AuthStatusDto
import dev.jdtech.jellyfin.models.Server
import dev.jdtech.jellyfin.models.ServerWithAddresses
import dev.jdtech.jellyfin.models.User
import dev.jdtech.jellyfin.settings.domain.AppPreferences
import dev.jdtech.jellyfin.setup.domain.SetupRepository
import dev.jdtech.jellyfin.setup.presentation.MainDispatcherRule
import dev.jdtech.jellyfin.setup.presentation.TestSharedPreferences
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

class ServersViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun serverClickNavigatesHomeWhenPinLockIsDisabled() = runTest {
        val appPreferences = AppPreferences(TestSharedPreferences())
        val repository = FakeSetupRepository(authStatus = authStatus(pinEnabled = false))
        val viewModel = ServersViewModel(repository = repository, appPreferences = appPreferences)

        viewModel.onAction(ServersAction.OnServerClick(serverId = "server-id"))

        assertEquals(ServersEvent.NavigateHome, viewModel.events.first())
        assertEquals("server-id", repository.currentServerId)
        assertEquals("server-id", appPreferences.getValue(appPreferences.currentServer))
    }

    @Test
    fun serverClickNavigatesAuthLockWhenPinLockIsEnabledAndLocked() = runTest {
        val viewModel =
            ServersViewModel(
                repository = FakeSetupRepository(authStatus = authStatus(pinEnabled = true)),
                appPreferences = AppPreferences(TestSharedPreferences()),
            )

        viewModel.onAction(ServersAction.OnServerClick(serverId = "server-id"))

        assertEquals(ServersEvent.NavigateAuthLock, viewModel.events.first())
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
        var currentServerId: String? = null

        override fun discoverServers(): Flow<ServerDiscoveryInfo> = emptyFlow()

        override suspend fun getServers(): List<ServerWithAddresses> = emptyList()

        override suspend fun getCurrentServer(): Server? = null

        override suspend fun deleteServer(serverId: String) = Unit

        override suspend fun getIsQuickConnectEnabled(): Boolean = false

        override suspend fun initiateQuickConnect(): QuickConnectResult = TODO()

        override suspend fun getQuickConnectState(secret: String): QuickConnectResult = TODO()

        override suspend fun setCurrentServer(serverId: String) {
            currentServerId = serverId
        }

        override suspend fun addServer(address: String): Server = TODO()

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
