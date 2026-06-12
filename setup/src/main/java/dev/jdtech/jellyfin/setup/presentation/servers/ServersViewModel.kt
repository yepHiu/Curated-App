package dev.jdtech.jellyfin.setup.presentation.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.curated.api.requiresUnlock
import dev.jdtech.jellyfin.settings.domain.AppPreferences
import dev.jdtech.jellyfin.setup.domain.SetupRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class ServersViewModel
@Inject
constructor(private val repository: SetupRepository, private val appPreferences: AppPreferences) :
    ViewModel() {
    private val _state = MutableStateFlow(ServersState())
    val state = _state.asStateFlow()

    private val eventsChannel = Channel<ServersEvent>()
    val events = eventsChannel.receiveAsFlow()

    fun loadServers() {
        viewModelScope.launch {
            val servers = repository.getServers()
            _state.emit(ServersState(servers = servers))
        }
    }

    private fun setCurrentServer(serverId: String) {
        viewModelScope.launch {
            repository.setCurrentServer(serverId)

            appPreferences.setValue(appPreferences.currentServer, serverId)

            eventsChannel.send(resolvePostServerSelectionEvent())
        }
    }

    private fun setCurrentAddress(addressId: UUID) {
        viewModelScope.launch {
            repository.setCurrentAddress(addressId)

            eventsChannel.send(ServersEvent.AddressChanged)
        }
    }

    private fun deleteServer(serverId: String) {
        viewModelScope.launch {
            repository.deleteServer(serverId)
            loadServers()
        }
    }

    fun onAction(action: ServersAction) {
        when (action) {
            is ServersAction.OnServerClick -> {
                setCurrentServer(action.serverId)
            }
            is ServersAction.OnAddressClick -> {
                setCurrentAddress(action.addressId)
            }
            is ServersAction.DeleteServer -> {
                deleteServer(action.serverId)
            }
            else -> Unit
        }
    }

    private suspend fun resolvePostServerSelectionEvent(): ServersEvent =
        try {
            if (repository.getCuratedAuthStatus().requiresUnlock()) {
                ServersEvent.NavigateAuthLock
            } else {
                ServersEvent.NavigateHome
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Unable to check Curated auth status after server selection")
            ServersEvent.NavigateHome
        }
}
