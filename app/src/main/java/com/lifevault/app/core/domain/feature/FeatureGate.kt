package com.lifevault.app.core.domain.feature

/**
 * Section 1.1's Free vs Pro table as pure functions of `isPro`. Security and
 * data-safety features (app lock, encryption, manual backup) are never gated here —
 * they're unconditionally available, per the spec's explicit trust rule.
 */
object FeatureGate {
    const val FREE_MAX_DOCUMENTS = 25
    const val FREE_MAX_FILES_PER_DOCUMENT = 3
    const val PRO_SOFT_CAP_FILES_PER_DOCUMENT = 50
    const val FREE_MAX_REMINDERS_PER_DOCUMENT = 2
    const val PRO_MAX_REMINDERS_PER_DOCUMENT = 6
    const val PRO_MAX_REMINDER_OFFSET_DAYS = 365

    /** The only offsets Free users may choose from (Section 1.1). */
    val FREE_REMINDER_OFFSET_PRESETS = listOf(90, 60, 30, 15, 7, 3, 1, 0)

    fun canAddDocument(currentCount: Int, isPro: Boolean): Boolean =
        isPro || currentCount < FREE_MAX_DOCUMENTS

    fun canAddFile(currentFileCount: Int, isPro: Boolean): Boolean =
        currentFileCount < if (isPro) PRO_SOFT_CAP_FILES_PER_DOCUMENT else FREE_MAX_FILES_PER_DOCUMENT

    fun canAddReminder(currentReminderCount: Int, isPro: Boolean): Boolean =
        currentReminderCount < if (isPro) PRO_MAX_REMINDERS_PER_DOCUMENT else FREE_MAX_REMINDERS_PER_DOCUMENT

    /** Free users must pick from [FREE_REMINDER_OFFSET_PRESETS]; Pro can use any 0..365. */
    fun isReminderOffsetAllowed(offsetDays: Int, isPro: Boolean): Boolean =
        if (isPro) offsetDays in 0..PRO_MAX_REMINDER_OFFSET_DAYS else offsetDays in FREE_REMINDER_OFFSET_PRESETS

    fun canSetReminderTimeOfDay(isPro: Boolean): Boolean = isPro

    fun canUseCustomCategories(isPro: Boolean): Boolean = isPro
    fun canUseOverdueNudges(isPro: Boolean): Boolean = isPro
    fun canUseRecurrence(isPro: Boolean): Boolean = isPro
    fun canTrackRenewalCost(isPro: Boolean): Boolean = isPro
    fun canUseScheduledAutoBackup(isPro: Boolean): Boolean = isPro
    fun canUseMergeRestore(isPro: Boolean): Boolean = isPro
    fun canExportCombinedPdf(isPro: Boolean): Boolean = isPro
    fun canUseWidget(isPro: Boolean): Boolean = isPro
    fun canUseDynamicColor(isPro: Boolean): Boolean = isPro
}
