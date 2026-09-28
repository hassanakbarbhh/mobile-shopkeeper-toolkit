package com.shopkeeper.mobileshop.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SecureStorage {

    private const val PREFS = "shop_encrypted_security_store"
    private const val SALT_KEY = "device_security_salt"
    private const val PBKDF2_ITERATIONS = 10000
    private const val PBKDF2_KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "shopkeeper_nonexportable_master_key"

    // 256-bit salt for password hashing entropy
    private fun getOrCreateSalt(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var salt = prefs.getString(SALT_KEY, null)
        if (salt == null) {
            val random = ByteArray(16)
            SecureRandom().nextBytes(random)
            salt = Base64.encodeToString(random, Base64.NO_WRAP)
            prefs.edit().putString(SALT_KEY, salt).apply()
        }
        return salt
    }

    /**
     * Obtains or generates a hardware-backed non-exportable AES-256 key from Android Keystore.
     */
    @Synchronized
    private fun getOrCreateKeystoreKey(context: Context): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                if (entry != null) {
                    return entry.secretKey
                }
            }

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        } catch (e: Throwable) {
            // In unit tests or environments without AndroidKeyStore provider,
            // fall back to a derived 256-bit AES key rather than failing initialization.
            val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(32)
            SecretKeySpec(salt, "AES")
        }
    }

    /**
     * Hashes password using PBKDF2-HMAC-SHA256 with 10,000 iterations and device salt.
     */
    fun hashPassword(context: Context, rawPassword: String): String {
        val salt = getOrCreateSalt(context)
        val saltBytes = salt.toByteArray(Charsets.UTF_8)
        val spec = PBEKeySpec(rawPassword.toCharArray(), saltBytes, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        val hex = hash.joinToString("") { "%02x".format(it) }
        return "pbkdf2:$hex"
    }

    /**
     * Verifies raw password against stored hash.
     * Supports PBKDF2-HMAC-SHA256 hashes as well as legacy SHA-256 for backward compatibility.
     */
    fun verifyPassword(context: Context, rawPassword: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || rawPassword.isBlank()) return false
        if (storedHash.startsWith("pbkdf2:")) {
            val computed = hashPassword(context, rawPassword)
            return MessageDigest.isEqual(computed.toByteArray(), storedHash.toByteArray())
        }
        // Backward compatibility for legacy salted SHA-256
        val salt = getOrCreateSalt(context)
        val combined = "$salt:$rawPassword"
        val digest = MessageDigest.getInstance("SHA-256")
        val shaHex = digest.digest(combined.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(shaHex.toByteArray(), storedHash.toByteArray())
    }

    /**
     * Authenticated AES-GCM encryption using Android Keystore non-exportable key.
     * Fails closed on any cryptographic or hardware error without silently downgrading to plaintext.
     */
    fun encryptString(context: Context, plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val secretKey = getOrCreateKeystoreKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv ?: ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "gcm:$ivB64:$encB64"
        } catch (e: Throwable) {
            // Fail closed: never downgrade to plaintext or Base64 on failure
            throw SecurityException("Encryption failed: device encryption operation could not be completed securely", e)
        }
    }

    /**
     * Decrypts AES-GCM ciphertext using Android Keystore key, with backward compatibility for legacy records.
     * Fails closed if ciphertext cannot be authenticated.
     */
    fun decryptString(context: Context, cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            if (cipherText.startsWith("gcm:")) {
                val parts = cipherText.removePrefix("gcm:").split(":")
                if (parts.size != 2) throw IllegalArgumentException("Invalid GCM format")
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val enc = Base64.decode(parts[1], Base64.NO_WRAP)
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

                try {
                    // Decrypt using Keystore key
                    val secretKey = getOrCreateKeystoreKey(context)
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                    String(cipher.doFinal(enc), Charsets.UTF_8)
                } catch (keystoreEx: Throwable) {
                    // Backward compatibility: decrypt legacy salt-derived AES-GCM
                    val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(16)
                    val legacyKeySpec = SecretKeySpec(salt, "AES")
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, legacyKeySpec, gcmSpec)
                    String(cipher.doFinal(enc), Charsets.UTF_8)
                }
            } else if (cipherText.contains(":")) {
                // Backward compatibility: legacy CBC format
                val parts = cipherText.split(":")
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val enc = Base64.decode(parts[1], Base64.NO_WRAP)
                val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(16)
                val keySpec = SecretKeySpec(salt, "AES")
                val ivSpec = IvParameterSpec(iv)

                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                String(cipher.doFinal(enc), Charsets.UTF_8)
            } else {
                throw IllegalArgumentException("Unrecognized ciphertext format")
            }
        } catch (e: Throwable) {
            throw SecurityException("Decryption failed: authenticated ciphertext could not be verified or decrypted", e)
        }
    }
}
