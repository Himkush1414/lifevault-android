package com.lifevault.app.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe routes for every screen in Section 3.2's inventory (S02–S24 — S01 Splash is
 * the system splash screen, not a nav destination; S11 Add-source and S14 Reminder
 * editor are modal bottom sheets, routed the same way as full screens here and
 * presented as sheets by the NavHost in Step 7's follow-up work).
 */
object Routes {

    @Serializable
    data object Onboarding

    @Serializable
    data object Permissions

    @Serializable
    data object LockSetup

    @Serializable
    data object Lock

    @Serializable
    data object Home

    @Serializable
    data class Documents(val categoryId: String? = null, val status: String? = null)

    @Serializable
    data object Categories

    @Serializable
    data class DocumentDetail(val documentId: String)

    @Serializable
    data class Viewer(val documentId: String, val startIndex: Int = 0)

    @Serializable
    data object AddSource

    @Serializable
    data class CaptureReview(val sessionId: String)

    @Serializable
    data class Editor(val documentId: String? = null, val sessionId: String? = null)

    @Serializable
    data class ReminderEditor(val documentId: String)

    @Serializable
    data object Deadlines

    @Serializable
    data object Reminders

    @Serializable
    data object Search

    @Serializable
    data object Settings

    @Serializable
    data object SecuritySettings

    @Serializable
    data object Backup

    @Serializable
    data class Restore(val uri: String)

    @Serializable
    data class Paywall(val trigger: String)

    @Serializable
    data object Trash

    @Serializable
    data object About
}
