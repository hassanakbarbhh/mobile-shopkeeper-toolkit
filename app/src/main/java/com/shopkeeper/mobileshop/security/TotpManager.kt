package com.shopkeeper.mobileshop.security

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpManager {

    private const val VALIDITY_PERIOD = 30000L // 30 seconds

    fun generateTotp(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        try {
            val timeStep = timeMillis / VALIDITY_PERIOD
            val msg = ByteBuffer.allocate(8).putLong(timeStep).array()
            
            // Simplified secret derivation for demo purposes
            val key = secret.toByteArray()
            
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(msg)
            
            val offset = hash[hash.size - 1].toInt() and 0xF
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            
            val otp = binary % 10.0.pow(6).toInt()
            return otp.toString().padStart(6, '0')
        } catch (e: Exception) {
            e.printStackTrace()
            return "000000"
        }
    }

    fun verifyTotp(secret: String, code: String): Boolean {
        if (code.isBlank()) return false
        val currentTime = System.currentTimeMillis()
        val currentTotp = generateTotp(secret, currentTime)
        val prevTotp = generateTotp(secret, currentTime - VALIDITY_PERIOD)
        
        return code == currentTotp || code == prevTotp
    }
}
