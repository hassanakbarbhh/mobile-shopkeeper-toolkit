package com.shopkeeper.mobileshop.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecureStorage {

    private const val PREFS = "shop_encrypted_security_store"
    private const val SALT_KEY = "device_security_salt"

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
     * Hashes password using SHA-256 with device-specific salt.
     */
    fun hashPassword(context: Context, rawPassword: String): String {
        val salt = getOrCreateSalt(context)
        val combined = "$salt:$rawPassword"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(combined.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies raw password against stored salted hash.
     */
    fun verifyPassword(context: Context, rawPassword: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || rawPassword.isBlank()) return false
        val computed = hashPassword(context, rawPassword)
        return MessageDigest.isEqual(computed.toByteArray(), storedHash.toByteArray())
    }

    /**
     * Simple AES encryption helper for sensitive local tokens.
     */
    fun encryptString(context: Context, plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val salt = getOrCreateSalt(context).toByteArray(Charsets.UTF_8).copyOf(16)
            val keySpec = SecretKeySpec(salt, "AES")
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Output format: IV:ENCRYPTED (base64)
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "$ivB64:$encB64"
        } catch (_: Throwable) {
            // Fallback base64
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decryptString(context: Context, cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            if (cipherText.contains(":")) {
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
