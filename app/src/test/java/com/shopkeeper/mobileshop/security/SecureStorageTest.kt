package com.shopkeeper.mobileshop.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecureStorageTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSaltedPasswordHashingAndVerification() {
        val pass = "SuperSecretPassword123"
        val hash = SecureStorage.hashPassword(context, pass)
        assertTrue(hash.isNotEmpty())
        assertTrue("Hash must use PBKDF2 prefix", hash.startsWith("pbkdf2:"))

        assertTrue(SecureStorage.verifyPassword(context, pass, hash))
        assertFalse(SecureStorage.verifyPassword(context, "WrongPassword", hash))
    }

    @Test
    fun testEncryptionFailsClosedWhenKeystoreUnavailable() {
        val original = "sensitive_session_token_xyz"
        // In JVM/Robolectric test environment where hardware AndroidKeyStore is unavailable,
        // encryptString must fail closed by throwing SecurityException rather than falling back to software key.
        val ex = assertThrows(SecurityException::class.java) {
            SecureStorage.encryptString(context, original)
        }
        assertTrue(
            "Must fail closed on Keystore unavailability",
            ex.message?.contains("device encryption operation could not be completed securely") == true
        )
    }

    @Test
    fun testEncryptionFailsClosedOnCorruptedCiphertext() {
        // Corrupted GCM ciphertext should fail closed by throwing SecurityException rather than returning plaintext
        assertThrows(SecurityException::class.java) {
            SecureStorage.decryptString(context, "gcm:corrupted_iv_base64:corrupted_ciphertext_base64")
        }
    }

    @Test
    fun testEncryptionFailsClosedOnArbitraryPlaintextInput() {
        // Arbitrary plain string that is not valid ciphertext must fail closed
        assertThrows(SecurityException::class.java) {
            SecureStorage.decryptString(context, "plain_unencrypted_attacker_input")
        }
    }

    @Test
    fun testEmptyStringEncryptionHandling() {
        assertEquals("", SecureStorage.encryptString(context, ""))
        assertEquals("", SecureStorage.decryptString(context, ""))
    }
}
