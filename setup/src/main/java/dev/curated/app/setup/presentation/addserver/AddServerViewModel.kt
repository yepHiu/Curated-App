package dev.curated.app.setup.presentation.addserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.curated.app.core.R as CoreR
import dev.curated.app.curated.api.requiresUnlock
import dev.curated.app.models.DiscoveredServer
import dev.curated.app.models.ExceptionUiText
import dev.curated.app.models.ExceptionUiTexts
import dev.curated.app.models.UiText
import dev.curated.app.settings.domain.AppPreferences
import dev.curated.app.setup.domain.SetupRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class AddServerViewModel
@Inject
constructor(private val repository: SetupRepository, private val appPreferences: AppPreferences) :
    ViewModel() {
    private val _state = MutableStateFlow(AddServerState())
    val state = _state.asStateFlow()

    private val eventsChannel = Channel<AddServerEvent>()
    val events = eventsChannel.receiveAsFlow()

    fun discoverServers() {
        viewModelScope.launch {
            val discoveredServers = mutableListOf<DiscoveredServer>()
            val serversDiscovery = repository.discoverServers()
            serversDiscovery.collect { serverDiscoveryInfo ->
                discoveredServers.add(
                    DiscoveredServer(
                        serverDiscoveryInfo.id,
                        serverDiscoveryInfo.name,
                        serverDiscoveryInfo.address,
                    )
                )
                _state.emit(_state.value.copy(discoveredServers = discoveredServers))
            }
        }
    }

    private fun connectToServer(address: String) {
        viewModelScope.launch {
            _state.emit(_state.value.copy(isLoading = true, error = null))

            try {
                val server = repository.addServer(address)
                appPreferences.setValue(appPreferences.currentServer, server.id)
                _state.emit(_state.value.copy(isLoading = false, error = null))
                eventsChannel.send(resolvePostConnectionEvent())
            } catch (_: CancellationException) {} catch (e: ExceptionUiText) {
                _state.emit(_state.value.copy(isLoading = false, error = listOf(e.uiText)))
            } catch (e: ExceptionUiTexts) {
                _state.emit(_state.value.copy(isLoading = false, error = e.uiTexts))
            } catch (e: Exception) {
                _state.emit(
                    _state.value.copy(
                        isLoading = false,
                        error =
                            listOf(
                                if (e.message != null) UiText.DynamicString(e.message!!)
                                else UiText.StringResource(CoreR.string.unknown_error)
                            ),
                    )
                )
            }
        }
    }

    fun onAction(action: AddServerAction) {
        when (action) {
            is AddServerAction.OnAddressChange -> {
                _state.value = _state.value.copy(serverAddress = action.address, error = null)
            }
            is AddServerAction.OnConnectClick -> {
                connectToServer(address = action.address)
            }
            else -> Unit
        }
    }

    private suspend fun resolvePostConnectionEvent(): AddServerEvent =
        try {
            if (repository.getCuratedAuthStatus().requiresUnlock()) {
                AddServerEvent.NavigateAuthLock
            } else {
                AddServerEvent.NavigateHome
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Unable to check Curated auth status after connecting")
            AddServerEvent.NavigateHome
        }
}
