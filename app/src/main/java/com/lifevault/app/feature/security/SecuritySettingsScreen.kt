package com.lifevault.app.feature.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.spacing.Spacing
import com.lifevault.app.core.domain.lock.AUTO_LOCK_TIMEOUT_OPTIONS_SECONDS

/**
 * S19 (Section 3.3): app lock, biometrics, auto-lock timeout, screenshot blocking.
 * "App lock off requires current PIN" and full change-PIN (current->new->confirm) are
 * simplified for now — TODO(step 8 polish): gate disabling behind PIN re-entry.
 */
@Composable
fun SecuritySettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SecuritySettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.securityState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
    ) {
        SettingsSwitchRow(
            title = "App lock",
            checked = state.appLockEnabled,
            onCheckedChange = { enabled -> if (!enabled) viewModel.disableAppLock() },
        )
        SettingsSwitchRow(
            title = "Unlock with biometrics",
            checked = state.biometricEnabled,
            enabled = state.appLockEnabled,
            onCheckedChange = viewModel::setBiometricEnabled,
        )
        AutoLockTimeoutSection(
            selectedSeconds = state.autoLockTimeoutSec,
            onSelect = viewModel::setAutoLockTimeoutSeconds,
        )
        SettingsSwitchRow(
            title = "Block screenshots & hide in recents",
            checked = state.blockScreenshots,
            onCheckedChange = viewModel::setBlockScreenshots,
        )
        Text(
            text = "Your data is encrypted with a key stored in this phone's secure " +
                "hardware. If you uninstall the app or clear its data, it cannot be " +
                "recovered without a backup.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun AutoLockTimeoutSection(selectedSeconds: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Auto-lock after", style = MaterialTheme.typography.titleSmall)
        val labels = remember {
            AUTO_LOCK_TIMEOUT_OPTIONS_SECONDS.associateWith {
                when (it) {
                    0 -> "Immediately"
                    60 -> "1 min"
                    300 -> "5 min"
                    else -> "${it}s"
                }
            }
        }
        for (seconds in AUTO_LOCK_TIMEOUT_OPTIONS_SECONDS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = labels.getValue(seconds), style = MaterialTheme.typography.bodyLarge)
                androidx.compose.material3.RadioButton(
                    selected = seconds == selectedSeconds,
                    onClick = { onSelect(seconds) },
                )
            }
        }
    }
}
