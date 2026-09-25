package com.lifevault.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Every route from [Routes] wired to [PlaceholderScreen] (Section 12 step 7). Each
 * later step (9, 11–24) swaps its route's placeholder for the real screen composable
 * here — the route itself, and everything that navigates to it, doesn't change.
 */
@Composable
fun LifeVaultNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Routes.Home, modifier = modifier) {
        composable<Routes.Onboarding> { PlaceholderScreen("Onboarding") }
        composable<Routes.Permissions> { PlaceholderScreen("Permissions") }
        composable<Routes.LockSetup> { PlaceholderScreen("Lock setup") }
        composable<Routes.Lock> { PlaceholderScreen("Lock") }
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
        composable<Routes.SecuritySettings> { PlaceholderScreen("Security & app lock") }
        composable<Routes.Backup> { PlaceholderScreen("Backup & restore") }
        composable<Routes.Restore> { PlaceholderScreen("Restore") }
        composable<Routes.Paywall> { PlaceholderScreen("LifeVault Pro") }
        composable<Routes.Trash> { PlaceholderScreen("Trash") }
        composable<Routes.About> { PlaceholderScreen("About & licences") }
    }
}
