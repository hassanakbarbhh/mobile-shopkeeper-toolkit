package com.shopkeeper.mobileshop.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecuritySanitizerTest {

    @Test
    fun testSanitizeStripsControlCharsAndEscapesHtml() {
        val raw = "<script>alert('xss')</script> & safe"
        val sanitized = SecuritySanitizer.sanitize(raw)
        assertFalse(sanitized.contains("<script>"))
        assertTrue(sanitized.contains("&lt;script&gt;"))
        assertTrue(sanitized.contains("&amp;"))
    }

    @Test
    fun testValidEmail() {
        assertTrue(SecuritySanitizer.isValidEmail("owner@mobileshop.com"))
        assertTrue(SecuritySanitizer.isValidEmail("staff.repair@shop.pk"))
        assertFalse(SecuritySanitizer.isValidEmail("invalid-email"))
        assertFalse(SecuritySanitizer.isValidEmail("@missingusername.com"))
        assertFalse(SecuritySanitizer.isValidEmail(""))
    }

    @Test
    fun testValidPhone() {
        assertTrue(SecuritySanitizer.isValidPhone("+14155552671"))
        assertTrue(SecuritySanitizer.isValidPhone("+923001234567"))
        assertFalse(SecuritySanitizer.isValidPhone("12345"))
        assertFalse(SecuritySanitizer.isValidPhone("no-plus-prefix"))
    }

    @Test
    fun testPasswordStrength() {
        val (weak, _) = SecuritySanitizer.validatePasswordStrength("123")
        assertFalse(weak)

        val (valid, _) = SecuritySanitizer.validatePasswordStrength("Secret123#")
        assertTrue(valid)
    }
}
