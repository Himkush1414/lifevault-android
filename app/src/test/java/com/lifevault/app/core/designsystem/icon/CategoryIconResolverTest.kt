package com.lifevault.app.core.designsystem.icon

import com.lifevault.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryIconResolverTest {

    @Test
    fun `known icon keys resolve to their matching drawable`() {
        assertEquals(R.drawable.ic_badge, categoryIconRes("badge"))
        assertEquals(R.drawable.ic_flight, categoryIconRes("flight"))
        assertEquals(R.drawable.ic_laptop_mac, categoryIconRes("laptop"))
    }

    @Test
    fun `an unrecognised key falls back to the Other icon`() {
        assertEquals(LifeVaultIcons.Category.Other, categoryIconRes("not_a_real_key"))
    }
}
