package com.lifevault.app.core.navigation

import com.lifevault.app.core.designsystem.icon.LifeVaultIcons

/** Section 3.1: the 4 bottom-nav destinations, shown only on these top-level screens. */
enum class TopLevelDestination(val route: Any, val icon: Int, val label: String) {
    Home(Routes.Home, LifeVaultIcons.Core.Home, "Home"),
    Documents(Routes.Documents(), LifeVaultIcons.Core.Documents, "Documents"),
    Deadlines(Routes.Deadlines, LifeVaultIcons.Core.Deadlines, "Deadlines"),
    Settings(Routes.Settings, LifeVaultIcons.Core.Settings, "Settings"),
}
