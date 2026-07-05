package dev.curated.app.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.curated.app.core.R as CoreR
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.theme.spacings
import dev.curated.app.settings.R as SettingsR
import dev.curated.app.settings.presentation.models.PreferenceCategory

@Composable
fun SettingsCategoryCard(preference: PreferenceCategory, modifier: Modifier = Modifier) {
    SettingsBaseCard(
        preference = preference,
        onClick = { preference.onClick(preference) },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacings.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (preference.iconDrawableId != null) {
                Icon(
                    painter = painterResource(preference.iconDrawableId!!),
                    contentDescription = null,
                )
            } else {
                Spacer(modifier = Modifier.size(MaterialTheme.spacings.default))
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacings.default))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(preference.nameStringResource),
                    style = MaterialTheme.typography.titleMedium,
                )
                preference.descriptionStringRes?.let {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacings.extraSmall))
                    Text(
                        text = stringResource(id = it),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsCategoryCardPreview() {
    CuratedTheme {
        SettingsCategoryCard(
            preference =
                PreferenceCategory(
                    nameStringResource = SettingsR.string.settings_category_player,
                    iconDrawableId = CoreR.drawable.ic_play,
                )
        )
    }
}
