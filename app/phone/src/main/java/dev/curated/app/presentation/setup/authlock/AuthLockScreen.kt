package dev.curated.app.presentation.setup.authlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.setup.components.LoadingButton
import dev.curated.app.presentation.setup.components.RootLayout
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.setup.R as SetupR
import dev.curated.app.setup.presentation.authlock.AuthLockAction
import dev.curated.app.setup.presentation.authlock.AuthLockEvent
import dev.curated.app.setup.presentation.authlock.AuthLockState
import dev.curated.app.setup.presentation.authlock.AuthLockViewModel
import dev.curated.app.utils.ObserveAsEvents

@Composable
fun AuthLockScreen(
    onSuccess: () -> Unit,
    onChangeServerClick: () -> Unit,
    viewModel: AuthLockViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AuthLockEvent.Success -> onSuccess()
            AuthLockEvent.ChangeServer -> onChangeServerClick()
        }
    }

    AuthLockScreenLayout(state = state, onAction = viewModel::onAction)
}

@Composable
private fun AuthLockScreenLayout(state: AuthLockState, onAction: (AuthLockAction) -> Unit) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val doUnlock = { onAction(AuthLockAction.OnUnlockClick) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            focusRequester.requestFocus()
        }
    }

    RootLayout {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier.fillMaxHeight()
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 420.dp)
                    .align(Alignment.Center),
        ) {
            Image(
                painter = painterResource(id = CoreR.drawable.ic_banner),
                contentDescription = null,
                modifier = Modifier.width(250.dp).align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(SetupR.string.auth_lock),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.pin,
                onValueChange = { onAction(AuthLockAction.OnPinChange(it)) },
                label = { Text(text = stringResource(SetupR.string.auth_lock_pin_hint)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(onDone = { doUnlock() }),
                isError = state.error != null,
                enabled = !state.isLoading,
                supportingText = {
                    state.error?.let {
                        Text(
                            text = it.asString(context.resources),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.trustedForever,
                    onCheckedChange = {
                        onAction(AuthLockAction.OnTrustedForeverChange(it))
                    },
                    enabled = !state.isLoading,
                )
                Text(text = stringResource(SetupR.string.auth_lock_trust_device))
            }
            Spacer(modifier = Modifier.height(12.dp))
            LoadingButton(
                text = stringResource(SetupR.string.auth_lock_btn_unlock),
                onClick = { doUnlock() },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = { onAction(AuthLockAction.OnChangeServerClick) },
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(text = stringResource(SetupR.string.auth_lock_change_server))
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun AuthLockScreenLayoutPreview() {
    CuratedTheme {
        AuthLockScreenLayout(state = AuthLockState(isLoading = false), onAction = {})
    }
}
