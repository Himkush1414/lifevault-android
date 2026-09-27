package com.lifevault.app.feature.editor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EditorUiStateTest {

    private val today = LocalDate.of(2026, 9, 25)

    @Test
    fun `canSave is false for a blank title`() {
        assertFalse(EditorUiState(title = "").canSave)
        assertFalse(EditorUiState(title = "   ").canSave)
    }

    @Test
    fun `canSave is true once a title is entered`() {
        assertTrue(EditorUiState(title = "Passport").canSave)
    }

    @Test
    fun `no expiry-past warning when noExpiry is set`() {
        val state = EditorUiState(noExpiry = true, expiryDate = today.minusDays(5), today = today)
        assertFalse(state.expiryAlreadyPastWarning)
    }

    @Test
    fun `expiry-past warning fires for a past date`() {
        val state = EditorUiState(noExpiry = false, expiryDate = today.minusDays(1), today = today)
        assertTrue(state.expiryAlreadyPastWarning)
    }

    @Test
    fun `no expiry-past warning for a future date`() {
        val state = EditorUiState(noExpiry = false, expiryDate = today.plusDays(1), today = today)
        assertFalse(state.expiryAlreadyPastWarning)
    }

    @Test
    fun `isEditingExisting reflects whether a documentId is present`() {
        assertTrue(EditorUiState(documentId = "abc").isEditingExisting)
        assertFalse(EditorUiState(documentId = null).isEditingExisting)
    }
}
