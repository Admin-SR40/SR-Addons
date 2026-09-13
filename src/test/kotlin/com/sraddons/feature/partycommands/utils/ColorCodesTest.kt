package com.sraddons.feature.partycommands.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class ColorCodesTest {
    @Test
    fun `strips section sign color codes`() {
        assertEquals("Test", "\u00a7aTest".noControlCodes)
        assertEquals("Test", "\u00a7b\u00a7lTest".noControlCodes)
        assertEquals("Test", "\u00a7ATest".noControlCodes)
    }

    @Test
    fun `strips ampersand color codes`() {
        assertEquals("Test", "&aTest".noControlCodes)
        assertEquals("Test", "&1Test".noControlCodes)
        assertEquals("Test", "&2Test".noControlCodes)
        assertEquals("Test", "&a&lTest".noControlCodes)
    }

    @Test
    fun `strips hex color codes`() {
        assertEquals("Test", "\u00a7x\u00a71\u00a72\u00a73\u00a74\u00a75\u00a76Test".noControlCodes)
        assertEquals("Test", "&#112233Test".noControlCodes)
    }

    @Test
    fun `keeps plain text untouched`() {
        assertEquals("Party > Admin_SR40: hello", "Party > Admin_SR40: hello".noControlCodes)
    }

    @Test
    fun `plain chat message removes codes and collapses whitespace`() {
        assertEquals("CMD >> Test", "\u00a7aCMD >> \u00a7fTest".toPlainChatMessage())
        assertEquals("a b", "a\n b".toPlainChatMessage())
        assertEquals("", "\u00a7a".toPlainChatMessage())
        assertEquals("Tom & Jerry", "Tom & Jerry".toPlainChatMessage())
    }
}
