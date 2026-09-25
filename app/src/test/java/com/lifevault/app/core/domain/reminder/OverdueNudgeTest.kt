package com.lifevault.app.core.domain.reminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OverdueNudgeTest {

    private val expiry = LocalDate.of(2026, 9, 1)

    @Test
    fun `not due before expiry`() {
        assertFalse(isOverdueNudgeDue(expiry, null, expiry.minusDays(1)))
    }

    @Test
    fun `not due on the expiry date itself`() {
        assertFalse(isOverdueNudgeDue(expiry, null, expiry))
    }

    @Test
    fun `first nudge due exactly 7 days after expiry, same weekday`() {
        assertTrue(isOverdueNudgeDue(expiry, null, expiry.plusDays(7)))
    }

    @Test
    fun `not due on an off-cadence day between weekly marks`() {
        assertFalse(isOverdueNudgeDue(expiry, null, expiry.plusDays(3)))
    }

    @Test
    fun `second nudge due at 14 days if the first already fired at 7`() {
        assertTrue(isOverdueNudgeDue(expiry, expiry.plusDays(7), expiry.plusDays(14)))
    }

    @Test
    fun `not due again the same week once already nudged`() {
        assertFalse(isOverdueNudgeDue(expiry, expiry.plusDays(7), expiry.plusDays(7)))
    }

    @Test
    fun `due at the 12-week boundary`() {
        assertTrue(isOverdueNudgeDue(expiry, expiry.plusWeeks(11), expiry.plusWeeks(12)))
    }

    @Test
    fun `not due past the 12-week cutoff`() {
        assertFalse(isOverdueNudgeDue(expiry, expiry.plusWeeks(12), expiry.plusWeeks(13)))
    }

    @Test
    fun `reset lastOverdueNudgeAt (null) after expiry date changes re-arms the cadence`() {
        // Simulates the Section 7.2 reset rule: a new expiry date with no nudge history yet.
        val newExpiry = LocalDate.of(2026, 10, 1)
        assertTrue(isOverdueNudgeDue(newExpiry, null, newExpiry.plusDays(7)))
    }
}
