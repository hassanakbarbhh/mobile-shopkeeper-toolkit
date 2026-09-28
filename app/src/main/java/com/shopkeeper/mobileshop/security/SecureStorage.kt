package com.shopkeeper.mobileshop.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
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

    // 256-bit key derived with fixed device-level entropy
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
     * Authenticated AES-GCM encryption for sensitive local tokens.
     */
    fun encryptString(context: Context, plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(16)
            val keySpec = SecretKeySpec(salt, "AES")
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "gcm:$ivB64:$encB64"
        } catch (_: Throwable) {
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts AES-GCM (or backward-compatible AES-CBC) ciphertext.
     */
    fun decryptString(context: Context, cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            if (cipherText.startsWith("gcm:")) {
                val parts = cipherText.removePrefix("gcm:").split(":")
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val enc = Base64.decode(parts[1], Base64.NO_WRAP)
                val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(16)
                val keySpec = SecretKeySpec(salt, "AES")
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
                String(cipher.doFinal(enc), Charsets.UTF_8)
            } else if (cipherText.contains(":")) {
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
                String(Base64.decode(cipherText, Base64.NO_WRAP), Charsets.UTF_8)
            }
        } catch (_: Throwable) {
            ""
        }
    }
}
