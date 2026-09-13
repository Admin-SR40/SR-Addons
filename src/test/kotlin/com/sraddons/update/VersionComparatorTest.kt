package com.sraddons.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionComparatorTest {
    @Test
    fun `compares patch numbers numerically`() {
        assertTrue(VersionComparator.compare("1.7.10", "1.7.9") > 0)
        assertTrue(VersionComparator.compare("1.7.4", "1.7.5") < 0)
        assertEquals(0, VersionComparator.compare("1.7.5", "1.7.5"))
    }

    @Test
    fun `ignores a leading v and compares minor versions`() {
        assertEquals(0, VersionComparator.compare("v1.7.5", "1.7.5"))
        assertTrue(VersionComparator.compare("1.8", "1.7.5") > 0)
    }

    @Test
    fun `treats a release as newer than its pre-release`() {
        assertTrue(VersionComparator.compare("1.7.5", "1.7.5-debug") > 0)
        assertTrue(VersionComparator.compare("1.7.5-beta1", "1.7.5") < 0)
    }
}
