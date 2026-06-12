package dev.jdtech.jellyfin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.CuratedApiClient
import dev.jdtech.jellyfin.curated.api.requiresUnlock
import dev.jdtech.jellyfin.database.ServerDatabaseDao
import dev.jdtech.jellyfin.models.Server
import dev.jdtech.jellyfin.models.ServerWithAddressAndUser
import dev.jdtech.jellyfin.models.User
import dev.jdtech.jellyfin.settings.domain.AppPreferences
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val appPreferences: AppPreferences,
    private val database: ServerDatabaseDao,
    private val curatedHttpClient: OkHttpClient,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    sealed class UiState {
        data class Normal(val server: Server?, val user: User?) : UiState()

        data object Loading : UiState()
    }

    init {
        check()
    }

    private fun check() {
        viewModelScope.launch {
            _state.emit(MainState(isLoading = true))
            val currentServer = getCurrentServerWithAddressAndUser()
            val mainState =
                MainState(
                    isLoading = false,
                    hasServers = checkHasServers(),
                    hasCurrentServer = currentServer != null,
                    hasCurrentUser = currentServer?.user != null,
                    isOfflineMode = checkIsOfflineMode(),
                    isCuratedAuthLocked =
                        checkIsCuratedAuthLocked(currentServer?.address?.address),
                )
            _state.emit(mainState)
        }
    }

    fun loadServerAndUser() {
        viewModelScope.launch {
            val serverId = appPreferences.getValue(appPreferences.currentServer)
            serverId?.let { id ->
                database.getServerWithAddressAndUser(id)?.let { data ->
                    _uiState.emit(UiState.Normal(data.server, data.user))
                }
            }
        }
    }

    private fun checkHasServers(): Boolean {
        val nServers = database.getServersCount()
        return nServers > 0
    }

    private fun getCurrentServerWithAddressAndUser(): ServerWithAddressAndUser? =
        appPreferences.getValue(appPreferences.currentServer)?.let {
            database.getServerWithAddressAndUser(it)
        }

    private fun checkIsOfflineMode(): Boolean {
        return appPreferences.getValue(appPreferences.offlineMode)
    }

    private suspend fun checkIsCuratedAuthLocked(serverAddress: String?): Boolean {
        if (serverAddress.isNullOrBlank()) return false

        return withContext(Dispatchers.IO) {
            try {
                CuratedApiClient(baseUrl = serverAddress, client = curatedHttpClient)
                    .getAuthStatus()
                    .requiresUnlock()
            } catch (e: Exception) {
                Timber.d(e, "Current server did not provide Curated auth status")
                false
            }
        }
    }
}

data class MainState(
    val isLoading: Boolean = true,
    val hasServers: Boolean = false,
    val hasCurrentServer: Boolean = false,
    val hasCurrentUser: Boolean = false,
    val isOfflineMode: Boolean = false,
    val isCuratedAuthLocked: Boolean = false,
)
