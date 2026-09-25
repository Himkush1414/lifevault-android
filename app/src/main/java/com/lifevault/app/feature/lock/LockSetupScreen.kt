package com.lifevault.app.feature.lock

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.component.NumericKeypad
import com.lifevault.app.core.designsystem.component.PinDots
import com.lifevault.app.core.designsystem.spacing.Spacing

/** S04 (Section 3.3): the 3-step app-lock setup wizard. */
@Composable
fun LockSetupScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.step == LockSetupStep.DONE) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (state.step) {
            LockSetupStep.CREATE_PIN, LockSetupStep.CONFIRM_PIN -> PinEntryStep(state, viewModel)
            LockSetupStep.BIOMETRIC_OFFER -> BiometricOfferStep(viewModel)
            LockSetupStep.DONE -> Unit
        }
    }
}

@Composable
private fun PinEntryStep(state: LockSetupUiState, viewModel: LockSetupViewModel) {
    val title = if (state.step == LockSetupStep.CREATE_PIN) "Create a 6-digit PIN" else "Confirm your PIN"
    Text(text = title, style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.size(Spacing.xxl))
    PinDots(digitsEntered = state.digitsEntered)
    if (state.errorMessage != null) {
        Spacer(Modifier.size(Spacing.md))
        Text(
            text = state.errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Spacer(Modifier.size(Spacing.xxxl))
    NumericKeypad(onDigit = viewModel::onDigit, onBackspace = viewModel::onBackspace)
}

@Composable
private fun BiometricOfferStep(viewModel: LockSetupViewModel) {
    val activity = LocalActivity.current as? FragmentActivity

    LaunchedEffect(activity) {
        activity?.let(viewModel::checkBiometricAvailability)
    }

    Text(text = "Unlock with fingerprint or face", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.size(Spacing.xxl))
    Button(onClick = { activity?.let(viewModel::attemptEnableBiometrics) }) {
        Text("Enable")
    }
    TextButton(onClick = viewModel::skipBiometrics) {
        Text("Skip")
    }
}
