package com.lifevault.app.core.navigation

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination.Companion.hasRoute
import com.lifevault.app.feature.lock.ForgotPinViewModel
import com.lifevault.app.feature.lock.LockScreen
import com.lifevault.app.feature.lock.LockSetupScreen
import com.lifevault.app.feature.onboarding.OnboardingScreen
import com.lifevault.app.feature.onboarding.PermissionsScreen
import com.lifevault.app.feature.security.SecuritySettingsScreen

/**
 * Every route from [Routes] wired to [PlaceholderScreen] (Section 12 step 7). Each
 * later step (9, 11–24) swaps its route's placeholder for the real screen composable
 * here — the route itself, and everything that navigates to it, doesn't change.
 *
 * This is the app's composition root, so — unlike code under `feature`, which never
 * depends on `core.navigation` — it's the one place allowed to import every feature screen.
 */
@Composable
fun LifeVaultNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable<Routes.Onboarding> {
            OnboardingScreen(onFinished = { navController.navigate(Routes.Permissions) })
        }
        composable<Routes.Permissions> {
            val toLockSetup = { navController.navigate(Routes.LockSetup) }
            PermissionsScreen(onContinue = toLockSetup, onNotNow = toLockSetup)
        }
        composable<Routes.LockSetup> {
            // Only the onboarding chain (Onboarding -> Permissions -> LockSetup) should
            // land on Home when finished; reaching this route any other way (e.g. S19
            // "Change PIN") should just return to where it was opened from.
            val cameFromOnboarding = navController.previousBackStackEntry
                ?.destination?.hasRoute(Routes.Permissions::class) == true
            LockSetupScreen(
                onFinished = {
                    if (cameFromOnboarding) {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
            )
        }
        composable<Routes.Lock> {
            LockRouteContent(onForgotPinSucceeded = { navController.navigate(Routes.LockSetup) })
        }
        composable<Routes.Home> { PlaceholderScreen("Home") }
        composable<Routes.Documents> { PlaceholderScreen("Documents") }
        composable<Routes.Categories> { PlaceholderScreen("Categories") }
        composable<Routes.DocumentDetail> { PlaceholderScreen("Document detail") }
        composable<Routes.Viewer> { PlaceholderScreen("Viewer") }
        composable<Routes.AddSource> { PlaceholderScreen("Add document") }
        composable<Routes.CaptureReview> { PlaceholderScreen("Review") }
        composable<Routes.Editor> { PlaceholderScreen("Editor") }
        composable<Routes.ReminderEditor> { PlaceholderScreen("Remind me") }
        composable<Routes.Deadlines> { PlaceholderScreen("Deadlines") }
        composable<Routes.Reminders> { PlaceholderScreen("Reminders") }
        composable<Routes.Search> { PlaceholderScreen("Search") }
        composable<Routes.Settings> { PlaceholderScreen("Settings") }
        composable<Routes.SecuritySettings> { SecuritySettingsScreen() }
        composable<Routes.Backup> { PlaceholderScreen("Backup & restore") }
        composable<Routes.Restore> { PlaceholderScreen("Restore") }
        composable<Routes.Paywall> { PlaceholderScreen("LifeVault Pro") }
        composable<Routes.Trash> { PlaceholderScreen("Trash") }
        composable<Routes.About> { PlaceholderScreen("About & licences") }
    }
}

/** Wires S05's "Forgot PIN?" to Section 8.4's device-credential verification. */
@Composable
private fun LockRouteContent(onForgotPinSucceeded: () -> Unit) {
    val activity = LocalActivity.current as? FragmentActivity
    val authenticator = hiltViewModel<ForgotPinViewModel>().authenticator

    val legacyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) onForgotPinSucceeded()
    }

    LockScreen(
        onForgotPin = {
            if (activity == null) return@LockScreen
            if (authenticator.canUseBiometricPromptPath()) {
                authenticator.authenticateWithBiometricPrompt(activity) { success ->
                    if (success) onForgotPinSucceeded()
                }
            } else if (authenticator.hasDeviceScreenLock()) {
                authenticator.confirmDeviceCredentialIntent()?.let(legacyLauncher::launch)
            }
            // No device screen lock at all: Section 8.4 step 3 (Restore from backup /
            // Erase and start fresh) — TODO(step 20 - backup & restore UI).
        },
    )
}
