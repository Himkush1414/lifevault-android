package com.lifevault.app.core.navigation

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination.Companion.hasRoute
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.lifevault.app.feature.capture.AddSourceSheet
import com.lifevault.app.feature.capture.CaptureCoordinatorViewModel
import com.lifevault.app.feature.capture.CaptureImportResult
import com.lifevault.app.feature.capture.CaptureReviewScreen
import com.lifevault.app.feature.capture.CaptureSource
import com.lifevault.app.feature.detail.DocumentDetailScreen
import com.lifevault.app.feature.editor.EditorScreen
import com.lifevault.app.feature.editor.ReminderEditorSheet
import com.lifevault.app.feature.lock.ForgotPinViewModel
import com.lifevault.app.feature.lock.LockScreen
import com.lifevault.app.feature.lock.LockSetupScreen
import com.lifevault.app.feature.onboarding.OnboardingScreen
import com.lifevault.app.feature.onboarding.PermissionsScreen
import com.lifevault.app.feature.security.SecuritySettingsScreen
import com.lifevault.app.feature.trash.TrashScreen

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
        composable<Routes.DocumentDetail> {
            DocumentDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { documentId -> navController.navigate(Routes.Editor(documentId = documentId)) },
            )
        }
        composable<Routes.Viewer> { PlaceholderScreen("Viewer") }
        composable<Routes.AddSource> {
            AddSourceRouteContent(
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.CaptureReview(sessionId)) {
                        popUpTo(Routes.AddSource) { inclusive = true }
                    }
                },
                onManual = {
                    navController.navigate(Routes.Editor()) {
                        popUpTo(Routes.AddSource) { inclusive = true }
                    }
                },
                onUpgrade = { navController.navigate(Routes.Paywall(trigger = "add_source")) },
            )
        }
        composable<Routes.CaptureReview> {
            CaptureReviewScreen(
                onSaved = { documentId ->
                    navController.navigate(Routes.Editor(documentId = documentId)) {
                        popUpTo(Routes.Home)
                    }
                },
                onCancel = { navController.popBackStack() },
                onUpgrade = { navController.navigate(Routes.Paywall(trigger = "file_limit")) },
            )
        }
        composable<Routes.Editor> {
            EditorScreen(
                onSaved = { documentId ->
                    navController.navigate(Routes.DocumentDetail(documentId)) {
                        popUpTo<Routes.Editor> { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }
        composable<Routes.ReminderEditor> {
            ReminderEditorSheet(onDone = { navController.popBackStack() })
        }
        composable<Routes.Deadlines> { PlaceholderScreen("Deadlines") }
        composable<Routes.Reminders> { PlaceholderScreen("Reminders") }
        composable<Routes.Search> { PlaceholderScreen("Search") }
        composable<Routes.Settings> { PlaceholderScreen("Settings") }
        composable<Routes.SecuritySettings> { SecuritySettingsScreen() }
        composable<Routes.Backup> { PlaceholderScreen("Backup & restore") }
        composable<Routes.Restore> { PlaceholderScreen("Restore") }
        composable<Routes.Paywall> { PlaceholderScreen("LifeVault Pro") }
        composable<Routes.Trash> { TrashScreen(onBack = { navController.popBackStack() }) }
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

/**
 * Wires S11's add-source sheet to Section 6.2's ML Kit Document Scanner, Section 6.3's
 * Photo Picker, and Section 6.4's SAF PDF import. All three converge on
 * [CaptureCoordinatorViewModel], which turns whichever result comes back into a
 * [com.lifevault.app.core.capture.CaptureSession] for the review screen.
 */
@Composable
private fun AddSourceRouteContent(
    onSessionReady: (sessionId: String) -> Unit,
    onManual: () -> Unit,
    onUpgrade: () -> Unit,
) {
    val activity = LocalActivity.current
    val contentResolver = LocalContext.current.contentResolver
    val coordinator = hiltViewModel<CaptureCoordinatorViewModel>()
    val result by coordinator.result.collectAsState()

    LaunchedEffect(result) {
        val current = result ?: return@LaunchedEffect
        if (current is CaptureImportResult.Success) {
            onSessionReady(current.session.sessionId)
        }
        coordinator.consumeResult()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) coordinator.importImages(contentResolver, uris)
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { coordinator.importPdf(contentResolver, it) }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != android.app.Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            ?: return@rememberLauncherForActivityResult
        coordinator.importImages(contentResolver, scanResult.pages.orEmpty().map { it.imageUri })
    }

    AddSourceSheet(
        onSelect = { source ->
            when (source) {
                CaptureSource.SCAN -> activity?.let { launchScanner(it, scannerLauncher) }
                CaptureSource.PHOTOS -> galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
                CaptureSource.PDF -> pdfLauncher.launch(arrayOf("application/pdf"))
                CaptureSource.MANUAL -> onManual()
            }
        },
        onUpgrade = onUpgrade,
    )
}

/**
 * Section 6.2: builds the scan intent and launches it. The unavailable-module fallback
 * is simply doing nothing here — the sheet's Photos/PDF/Manual rows stay usable.
 */
private fun launchScanner(activity: android.app.Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setGalleryImportAllowed(false)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .build()
    GmsDocumentScanning.getClient(options).getStartScanIntent(activity)
        .addOnSuccessListener { intentSender ->
            launcher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
        .addOnFailureListener {
            // Section 6.2 unavailable fallback: no dialog spam — other sources stay usable.
        }
}
