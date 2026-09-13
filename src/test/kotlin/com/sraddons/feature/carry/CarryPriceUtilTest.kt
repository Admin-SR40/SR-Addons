package com.sraddons.feature.carry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CarryPriceUtilTest {
    @Test
    fun `parses suffixed amounts`() {
        assertEquals(1_500_000L, CarryPriceUtil.parsePrice("1.5M"))
        assertEquals(2_000_000L, CarryPriceUtil.parsePrice(" 2m "))
        assertEquals(500_000L, CarryPriceUtil.parsePrice("500K"))
    }

    @Test
    fun `parses plain integers`() {
        assertEquals(123L, CarryPriceUtil.parsePrice("123"))
        assertEquals(0L, CarryPriceUtil.parsePrice("0"))
    }

    @Test
    fun `rejects invalid input`() {
        assertNull(CarryPriceUtil.parsePrice(""))
        assertNull(CarryPriceUtil.parsePrice("abc"))
        assertNull(CarryPriceUtil.parsePrice("-5"))
        assertNull(CarryPriceUtil.parsePrice("-1M"))
        assertNull(CarryPriceUtil.parsePrice("1.5"))
    }

    @Test
    fun `formats prices back into readable amounts`() {
        assertEquals("2M", CarryPriceUtil.formatPrice(2_000_000L))
        assertEquals("1.5M", CarryPriceUtil.formatPrice(1_500_000L))
        assertEquals("1.2M", CarryPriceUtil.formatPrice(1_234_567L))
        assertEquals("1K", CarryPriceUtil.formatPrice(1_000L))
        assertEquals("500K", CarryPriceUtil.formatPrice(500_000L))
        assertEquals("999", CarryPriceUtil.formatPrice(999L))
    }
}
