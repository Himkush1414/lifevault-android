package com.lifevault.app.core.navigation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Requires a Compose UI test environment (a real device/emulator) — not executed in
 * this build environment (see BUILD_LOG.md Step 7). Written to run via
 * `./gradlew connectedDebugAndroidTest`. Covers Section 12 step 7's accept check: every
 * route is reachable, and back navigation returns to the previous screen.
 */
@RunWith(AndroidJUnit4::class)
class LifeVaultNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController

    private fun setContent() {
        composeRule.setContent {
            navController = TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
                .also { it.navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator()) }
            LifeVaultNavHost(navController = navController)
        }
    }

    @Test
    fun startDestinationIsHome() {
        setContent()
        composeRule.onNodeWithText("Home").assertExists()
    }

    @Test
    fun everyRouteIsReachable() {
        setContent()
        val routes = listOf(
            Routes.Onboarding to "Onboarding",
            Routes.Permissions to "Permissions",
            Routes.LockSetup to "Lock setup",
            Routes.Lock to "Lock",
            Routes.Categories to "Categories",
            Routes.AddSource to "Add document",
            Routes.Deadlines to "Deadlines",
            Routes.Reminders to "Reminders",
            Routes.Search to "Search",
            Routes.Settings to "Settings",
            Routes.SecuritySettings to "Security & app lock",
            Routes.Backup to "Backup & restore",
            Routes.Trash to "Trash",
            Routes.About to "About & licences",
        )
        for ((route, expectedTitle) in routes) {
            composeRule.runOnUiThread { navController.navigate(route) }
            composeRule.onNodeWithText(expectedTitle).assertExists()
        }
    }

    @Test
    fun backNavigationReturnsToThePreviousScreen() {
        setContent()

        composeRule.runOnUiThread { navController.navigate(Routes.Settings) }
        composeRule.onNodeWithText("Settings").assertExists()

        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.onNodeWithText("Home").assertExists()
    }
}
