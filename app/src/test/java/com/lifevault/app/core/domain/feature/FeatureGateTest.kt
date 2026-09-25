package com.lifevault.app.core.domain.feature

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureGateTest {

    // --- documents ---

    @Test
    fun `free can add documents below the 25 cap`() {
        assertTrue(FeatureGate.canAddDocument(currentCount = 24, isPro = false))
    }

    @Test
    fun `free cannot add the 26th document`() {
        assertFalse(FeatureGate.canAddDocument(currentCount = 25, isPro = false))
    }

    @Test
    fun `pro has no document cap`() {
        assertTrue(FeatureGate.canAddDocument(currentCount = 10_000, isPro = true))
    }

    // --- files per document ---

    @Test
    fun `free can add files below the 3 cap`() {
        assertTrue(FeatureGate.canAddFile(currentFileCount = 2, isPro = false))
    }

    @Test
    fun `free cannot add a 4th file`() {
        assertFalse(FeatureGate.canAddFile(currentFileCount = 3, isPro = false))
    }

    @Test
    fun `pro can add files up to the 50 soft cap`() {
        assertTrue(FeatureGate.canAddFile(currentFileCount = 49, isPro = true))
        assertFalse(FeatureGate.canAddFile(currentFileCount = 50, isPro = true))
    }

    // --- reminders per document ---

    @Test
    fun `free capped at 2 reminders`() {
        assertTrue(FeatureGate.canAddReminder(currentReminderCount = 1, isPro = false))
        assertFalse(FeatureGate.canAddReminder(currentReminderCount = 2, isPro = false))
    }

    @Test
    fun `pro capped at 6 reminders`() {
        assertTrue(FeatureGate.canAddReminder(currentReminderCount = 5, isPro = true))
        assertFalse(FeatureGate.canAddReminder(currentReminderCount = 6, isPro = true))
    }

    // --- reminder offsets ---

    @Test
    fun `free must choose a preset offset`() {
        assertTrue(FeatureGate.isReminderOffsetAllowed(30, isPro = false))
        assertFalse(FeatureGate.isReminderOffsetAllowed(45, isPro = false))
    }

    @Test
    fun `pro can choose any offset in 0 to 365`() {
        assertTrue(FeatureGate.isReminderOffsetAllowed(0, isPro = true))
        assertTrue(FeatureGate.isReminderOffsetAllowed(365, isPro = true))
        assertTrue(FeatureGate.isReminderOffsetAllowed(45, isPro = true))
        assertFalse(FeatureGate.isReminderOffsetAllowed(366, isPro = true))
        assertFalse(FeatureGate.isReminderOffsetAllowed(-1, isPro = true))
    }

    @Test
    fun `only pro can set a custom reminder time of day`() {
        assertFalse(FeatureGate.canSetReminderTimeOfDay(isPro = false))
        assertTrue(FeatureGate.canSetReminderTimeOfDay(isPro = true))
    }

    // --- Pro-only feature flags ---

    @Test
    fun `pro-only features are false for free and true for pro`() {
        val gates = listOf(
            FeatureGate::canUseCustomCategories,
            FeatureGate::canUseOverdueNudges,
            FeatureGate::canUseRecurrence,
            FeatureGate::canTrackRenewalCost,
            FeatureGate::canUseScheduledAutoBackup,
            FeatureGate::canUseMergeRestore,
            FeatureGate::canExportCombinedPdf,
            FeatureGate::canUseWidget,
            FeatureGate::canUseDynamicColor,
        )
        for (gate in gates) {
            assertFalse(gate(false))
            assertTrue(gate(true))
        }
    }
}
