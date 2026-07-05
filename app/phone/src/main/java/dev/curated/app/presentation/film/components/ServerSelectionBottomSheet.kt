package dev.curated.app.presentation.film.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.curated.app.core.R as CoreR
import dev.curated.app.core.presentation.dummy.dummyServer
import dev.curated.app.core.presentation.dummy.dummyServerAddress
import dev.curated.app.models.ServerWithAddresses
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings
import dev.curated.app.setup.presentation.servers.ServersAction
import dev.curated.app.setup.presentation.servers.ServersEvent
import dev.curated.app.setup.presentation.servers.ServersState
import dev.curated.app.setup.presentation.servers.ServersViewModel
import dev.curated.app.utils.ObserveAsEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionBottomSheet(
    currentServerId: String,
    onUpdate: () -> Unit,
    onManage: () -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    viewModel: ServersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(true) { viewModel.loadServers() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ServersEvent.NavigateHome -> onUpdate()
            is ServersEvent.NavigateAuthLock -> onUpdate()
            is ServersEvent.AddressChanged -> onUpdate()
        }
    }

    ServerSelectionBottomSheetLayout(
        currentServerId = currentServerId,
        state = state,
        onManage = onManage,
        onAction = { action -> viewModel.onAction(action) },
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerSelectionBottomSheetLayout(
    currentServerId: String,
    state: ServersState,
    onManage: () -> Unit,
    onAction: (action: ServersAction) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        LazyColumn(
            contentPadding =
                PaddingValues(
                    start = MaterialTheme.spacings.medium,
                    end = MaterialTheme.spacings.medium,
                    bottom = MaterialTheme.spacings.default,
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium),
        ) {
            items(items = state.servers, key = { it.server.id }) { server ->
                ServerSelectionItem(
                    server = server,
                    selected = server.server.id == currentServerId,
                    onClick = { onAction(ServersAction.OnServerClick(server.server.id)) },
                    onClickAddress = { addressId ->
                        onAction(ServersAction.OnAddressClick(addressId = addressId))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "manage") {
                OutlinedButton(onClick = onManage, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(CoreR.string.manage_servers))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun ServerSelectionBottomSheetPreview() {
    CuratedTheme {
        ServerSelectionBottomSheetLayout(
            currentServerId = "",
            state =
                ServersState(
                    servers =
                        listOf(
                            ServerWithAddresses(
                                server = dummyServer,
                                addresses = listOf(dummyServerAddress),
                                user = null,
                            )
                        )
                ),
            onManage = {},
            onAction = {},
            onDismissRequest = {},
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
        )
    }
}
