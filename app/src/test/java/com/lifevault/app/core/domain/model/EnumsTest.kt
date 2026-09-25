package com.lifevault.app.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumsTest {

    @Test
    fun `AttachmentKind has the two expected values`() {
        assertEquals(listOf(AttachmentKind.IMAGE, AttachmentKind.PDF), AttachmentKind.entries)
    }

    @Test
    fun `ThemeMode has the three expected values`() {
        assertEquals(
            listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
            ThemeMode.entries,
        )
    }

    @Test
    fun `BackupFrequency has the two expected values`() {
        assertEquals(listOf(BackupFrequency.WEEKLY, BackupFrequency.MONTHLY), BackupFrequency.entries)
    }
}
