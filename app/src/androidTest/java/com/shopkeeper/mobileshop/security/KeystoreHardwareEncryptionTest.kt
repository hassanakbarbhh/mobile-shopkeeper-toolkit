package com.shopkeeper.mobileshop.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test executing in the real Android environment where AndroidKeyStore provider is present.
 * Validates hardware-backed non-exportable AES-GCM encryption, decryption, and integrity guarantees.
 */
@RunWith(AndroidJUnit4::class)
class KeystoreHardwareEncryptionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testRealAndroidKeystoreEncryptionAndDecryptionRoundtrip() {
        val originalSecret = "production_credentials_secret_key_98765"
        val encrypted = SecureStorage.encryptString(context, originalSecret)

        assertNotEquals("Ciphertext must differ from plaintext", originalSecret, encrypted)
        assertTrue("Ciphertext must use authenticated AES-GCM scheme", encrypted.startsWith("gcm:"))

        val decrypted = SecureStorage.decryptString(context, encrypted)
        assertEquals("Decrypted plaintext must match original payload", originalSecret, decrypted)
    }

    @Test
    fun testKeystoreProducesDifferentIVsForSamePlaintext() {
        val plaintext = "identical_input_repeated"
        val cipher1 = SecureStorage.encryptString(context, plaintext)
        val cipher2 = SecureStorage.encryptString(context, plaintext)

        assertNotEquals("Randomized IVs must ensure different ciphertexts for identical inputs", cipher1, cipher2)
        assertEquals(plaintext, SecureStorage.decryptString(context, cipher1))
        assertEquals(plaintext, SecureStorage.decryptString(context, cipher2))
    }
}
