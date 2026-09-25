package com.lifevault.app.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * The app's root composable once unlocked (Section 3.1/3.4): adaptive bottom
 * bar/rail shown only on the 4 top-level destinations, wrapping a single [LifeVaultNavHost]
 * whose back stack (owned by the hoisted [rememberNavController]) survives the bar
 * appearing/disappearing around it.
 */
@Composable
fun LifeVaultAppScaffold(startDestination: Any) {
    LockGate {
        val navController = rememberNavController()
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        val isTopLevel = TopLevelDestination.entries.any { currentDestination.isTopLevelDestination(it) }

        if (isTopLevel) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    TopLevelDestination.entries.forEach { destination ->
                        item(
                            selected = currentDestination.isTopLevelDestination(destination),
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.icon),
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                },
            ) {
                LifeVaultNavHost(navController, startDestination)
            }
        } else {
            LifeVaultNavHost(navController, startDestination)
        }
    }
}
