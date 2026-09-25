package com.lifevault.app.feature.lock

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.component.NumericKeypad
import com.lifevault.app.core.designsystem.component.PinDots
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import kotlinx.coroutines.delay

/** S05 (Section 3.3): the lock screen. */
@Composable
fun LockScreen(
    onForgotPin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalActivity.current as? FragmentActivity

    LaunchedEffect(state.biometricEnabled, state.biometricAutoLaunchArmed, activity) {
        if (state.biometricEnabled && state.biometricAutoLaunchArmed && activity != null) {
            viewModel.attemptBiometric(activity)
        }
    }

    LaunchedEffect(state.lockoutRemainingMillis) {
        if (state.lockoutRemainingMillis > 0) {
            delay(1000)
            viewModel.refreshLockout()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(LifeVaultIcons.Core.Lock),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.size(Spacing.lg))
            Text(text = "LifeVault is locked", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(Spacing.xxl))

            if (state.lockoutRemainingMillis > 0) {
                Text(
                    text = "Try again in ${formatLockoutRemaining(state.lockoutRemainingMillis)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                PinDots(digitsEntered = state.digitsEntered)
                if (state.errorMessage != null) {
                    Spacer(Modifier.size(Spacing.md))
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.size(Spacing.xxxl))
                NumericKeypad(onDigit = viewModel::onDigit, onBackspace = viewModel::onBackspace)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (state.biometricEnabled && state.lockoutRemainingMillis == 0L) {
                IconButton(onClick = { activity?.let(viewModel::attemptBiometric) }) {
                    Icon(
                        painter = painterResource(LifeVaultIcons.Core.Fingerprint),
                        contentDescription = "Use biometrics",
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
            TextButton(onClick = onForgotPin) {
                Text("Forgot PIN?")
            }
        }
    }
}

private fun formatLockoutRemaining(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
