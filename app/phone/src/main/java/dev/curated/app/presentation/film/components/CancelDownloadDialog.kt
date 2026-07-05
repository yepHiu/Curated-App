package dev.curated.app.presentation.film.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.theme.CuratedTheme

@Composable
fun CancelDownloadDialog(onCancel: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        title = { Text(text = stringResource(CoreR.string.cancel_download)) },
        text = { Text(text = stringResource(CoreR.string.cancel_download_message)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(CoreR.string.stop_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(CoreR.string.cancel)) }
        },
    )
}

@Composable
@Preview
private fun CancelDownloadDialogPreview() {
    CuratedTheme { CancelDownloadDialog(onCancel = {}, onDismiss = {}) }
}
