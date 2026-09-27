package com.lifevault.app.core.designsystem.color

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryPaletteResolverTest {

    @Test
    fun `known color keys resolve case-insensitively`() {
        assertEquals(CategoryColorKey.Blue, categoryColorKeyOf("blue"))
        assertEquals(CategoryColorKey.Blue, categoryColorKeyOf("BLUE"))
        assertEquals(CategoryColorKey.Teal, categoryColorKeyOf("Teal"))
    }

    @Test
    fun `an unrecognised color key falls back to Grey`() {
        assertEquals(CategoryColorKey.Grey, categoryColorKeyOf("not_a_real_color"))
    }
}
