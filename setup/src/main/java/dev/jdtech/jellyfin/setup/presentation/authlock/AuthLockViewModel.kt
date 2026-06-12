package dev.jdtech.jellyfin.setup.presentation.authlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.curated.api.CuratedApiException
import dev.jdtech.jellyfin.curated.api.CuratedFailure
import dev.jdtech.jellyfin.curated.api.requiresUnlock
import dev.jdtech.jellyfin.models.ExceptionUiText
import dev.jdtech.jellyfin.models.UiText
import dev.jdtech.jellyfin.setup.R as SetupR
import dev.jdtech.jellyfin.setup.domain.SetupRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthLockViewModel @Inject constructor(private val repository: SetupRepository) :
    ViewModel() {
    private val _state = MutableStateFlow(AuthLockState())
    val state = _state.asStateFlow()

    private val eventsChannel = Channel<AuthLockEvent>()
    val events = eventsChannel.receiveAsFlow()

    init {
        refreshStatus()
    }

    fun onAction(action: AuthLockAction) {
        when (action) {
            is AuthLockAction.OnPinChange -> {
                _state.value =
                    _state.value.copy(
                        pin = sanitizeCuratedPinInput(action.pin, _state.value.pinLength),
                        error = null,
                    )
            }
            is AuthLockAction.OnTrustedForeverChange -> {
                _state.value = _state.value.copy(trustedForever = action.trustedForever)
            }
            AuthLockAction.OnUnlockClick -> unlock()
            AuthLockAction.OnChangeServerClick -> {
                viewModelScope.launch { eventsChannel.send(AuthLockEvent.ChangeServer) }
            }
        }
    }

    private fun refreshStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val status = repository.getCuratedAuthStatus()
                if (!status.requiresUnlock()) {
                    eventsChannel.send(AuthLockEvent.Success)
                    return@launch
                }
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        pinLength = status.pinLength.coerceAtLeast(MIN_PIN_LENGTH),
                    )
            } catch (e: CancellationException) {
                throw e
            } catch (e: ExceptionUiText) {
                _state.value = _state.value.copy(isLoading = false, error = e.uiText)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.toUiText())
            }
        }
    }

    private fun unlock() {
        viewModelScope.launch {
            val pin = _state.value.pin
            if (pin.isBlank()) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = UiText.StringResource(SetupR.string.auth_lock_error_pin_required),
                    )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val status =
                    repository.unlockCurated(
                        pin = pin,
                        trustedForever = _state.value.trustedForever,
                    )
                if (status.requiresUnlock()) {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error =
                                UiText.StringResource(
                                    SetupR.string.auth_lock_error_invalid_pin
                                ),
                        )
                    return@launch
                }
                eventsChannel.send(AuthLockEvent.Success)
            } catch (e: CancellationException) {
                throw e
            } catch (e: CuratedApiException) {
                _state.value = _state.value.copy(isLoading = false, error = e.toUiText())
            } catch (e: ExceptionUiText) {
                _state.value = _state.value.copy(isLoading = false, error = e.uiText)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.toUiText())
            }
        }
    }

    private fun Exception.toUiText(): UiText =
        if (message != null) {
            UiText.DynamicString(message!!)
        } else {
            UiText.StringResource(CoreR.string.unknown_error)
        }

    private fun CuratedApiException.toUiText(): UiText =
        when (failure.kind) {
            CuratedFailure.InvalidPin ->
                UiText.StringResource(SetupR.string.auth_lock_error_invalid_pin)
            CuratedFailure.AuthLocked ->
                UiText.StringResource(SetupR.string.auth_lock_error_locked)
            else -> failure.message.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
                ?: UiText.StringResource(CoreR.string.unknown_error)
        }
}

internal fun sanitizeCuratedPinInput(input: String, maxLength: Int): String =
    input.filter(Char::isDigit).take(maxLength.coerceAtLeast(0))

private const val MIN_PIN_LENGTH = 4
