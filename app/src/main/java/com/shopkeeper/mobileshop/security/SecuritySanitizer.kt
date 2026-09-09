package com.shopkeeper.mobileshop.security

import java.util.regex.Pattern

object SecuritySanitizer {

    // Self-contained email regex compatible with both Android and JVM
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$"
    )

    // E.164 phone format: + followed by 7 to 15 digits
    private val PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$")

    /**
     * Sanitizes plain user input:
     * - Trims whitespace
     * - Removes control characters (ASCII 0-31 except tab/newline)
     * - Escapes HTML/script tags to prevent injection
     */
    fun sanitize(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input.trim()
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    /**
     * Reverses HTML entity escaping for display when needed.
     */
    fun unescape(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#x27;", "'")
    }

    /**
     * Validates email format strictly.
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim()
        return clean.length in 5..120 && EMAIL_PATTERN.matcher(clean).matches()
    }

    /**
     * Validates E.164 international phone number format.
     */
    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return PHONE_PATTERN.matcher(clean).matches()
    }

    /**
     * Enforces password complexity requirements.
     * Minimum 6 characters (Firebase Auth requirement).
     */
    fun validatePasswordStrength(password: String?): Pair<Boolean, String> {
        val pass = password.orEmpty()
        if (pass.length < 6) {
            return Pair(false, "Password must be at least 6 characters long.")
        }
        if (pass.length > 128) {
            return Pair(false, "Password is too long (maximum 128 characters).")
        }
        return Pair(true, "Password is valid.")
    }
}
