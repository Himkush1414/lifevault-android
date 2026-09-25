package com.lifevault.app.core.domain.lock

import org.junit.Assert.assertEquals
import org.junit.Test

class PinRulesTest {

    @Test
    fun `a reasonable PIN is valid`() {
        assertEquals(PinValidationResult.Valid, validatePin("284719"))
    }

    @Test
    fun `wrong length is rejected`() {
        assertEquals(PinValidationResult.WrongLength, validatePin("1234"))
        assertEquals(PinValidationResult.WrongLength, validatePin("12345678"))
    }

    @Test
    fun `non-digit characters are rejected`() {
        assertEquals(PinValidationResult.NotAllDigits, validatePin("12a456"))
    }

    @Test
    fun `all same digit is rejected`() {
        assertEquals(PinValidationResult.AllSameDigit, validatePin("000000"))
        assertEquals(PinValidationResult.AllSameDigit, validatePin("999999"))
    }

    @Test
    fun `ascending sequence is rejected`() {
        assertEquals(PinValidationResult.SequentialDigits, validatePin("123456"))
        assertEquals(PinValidationResult.SequentialDigits, validatePin("456789"))
    }

    @Test
    fun `descending sequence is rejected`() {
        assertEquals(PinValidationResult.SequentialDigits, validatePin("654321"))
        assertEquals(PinValidationResult.SequentialDigits, validatePin("987654"))
    }

    @Test
    fun `a curated common PIN is rejected`() {
        assertEquals(PinValidationResult.CommonlyUsed, validatePin("123123"))
    }

    @Test
    fun `a near-sequential PIN with one break is allowed`() {
        // 123457 is not a straight run (breaks between 5 and 7) and isn't in the common list.
        assertEquals(PinValidationResult.Valid, validatePin("123457"))
    }
}
