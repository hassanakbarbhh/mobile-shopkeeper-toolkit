package com.shopkeeper.mobileshop.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

        assertTrue(SecureStorage.verifyPassword(context, pass, hash))
        assertFalse(SecureStorage.verifyPassword(context, "WrongPassword", hash))
    }

    @Test
    fun testEncryptionAndDecryption() {
        val original = "sensitive_session_token_xyz"
        val encrypted = SecureStorage.encryptString(context, original)
        assertTrue(encrypted != original)

        val decrypted = SecureStorage.decryptString(context, encrypted)
        assertEquals(original, decrypted)
    }
}
