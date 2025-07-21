package com.terrydroid.msgverify.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsScreen(
    paddingValues: PaddingValues,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        contentPadding = paddingValues
    ) {
        item {
            Spacer(Modifier.size(24.dp))
        }
        item {
            Text(
                text = "Overlay settings",
                style = MaterialTheme.typography.labelSmall
            )
        }
        item {
            Spacer(Modifier.size(8.dp))
        }
        item {
            val overlayEnabled =
                remember { mutableStateOf(false) } // TODO: Get default state from system
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(shape = MaterialTheme.shapes.small)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                    Text(
                        text = "Enable overlay on other apps",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Will demand system settings enabled",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Checkbox(
                    checked = overlayEnabled.value,
                    onCheckedChange = { checked ->
                        overlayEnabled.value = checked
                    }
                )
            }
        }

        item {
            Spacer(Modifier.size(24.dp))
        }

        item {
            Text(
                text = "Google Safe Browsing",
                style = MaterialTheme.typography.labelSmall
            )
        }
        item {
            Spacer(Modifier.size(8.dp))
        }

        item {
            val safeBrowserState by settingsViewModel.safeBrowserState.collectAsStateWithLifecycle()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.small)
                        .background(color = MaterialTheme.colorScheme.surfaceContainer)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                        Text(
                            text = "Enable Google SafeBrowsing",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "If this is enabled, the system will check against an " +
                                    "external list of hashed urls",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Checkbox(
                        checked = safeBrowserState.enabled,
                        onCheckedChange = { checked ->
                            settingsViewModel.toggleSafeBrowserState(checked)
                        },
                        enabled = safeBrowserState !is SafeBrowserState.Loading
                    )
                }
                when (safeBrowserState) {
                    is SafeBrowserState.Initial -> {
                        /* NO-OP */
                    }

                    is SafeBrowserState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Downloading urls",
                                style = MaterialTheme.typography.labelSmall
                            )
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                color = ProgressIndicatorDefaults.linearColor,
                                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                            )
                        }
                    }

                    is SafeBrowserState.Success -> {
                        /* NO-OP */
                    }

                    is SafeBrowserState.Error -> {
                        Text(
                            text = "Could not use Google Safe browsing, please check your connectivity"
                        )
                    }
                }
            }
        }
    }
}
