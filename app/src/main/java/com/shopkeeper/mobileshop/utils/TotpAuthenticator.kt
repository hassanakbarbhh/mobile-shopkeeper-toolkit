package com.shopkeeper.mobileshop.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.TimeUnit

class TotpAuthenticator {
    fun generateQrCodeForAuthenticator(secret: String, accountName: String, issuer: String): Bitmap {
        val uri = "otpauth://totp/$issuer:$accountName?secret=$secret&issuer=$issuer"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(uri, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun isDeviceTrusted(lastTrustDateMillis: Long): Boolean {
        val thirtyDaysMillis = TimeUnit.DAYS.toMillis(30)
        return (System.currentTimeMillis() - lastTrustDateMillis) <= thirtyDaysMillis
    }

    fun verifyTotpCode(secret: String, code: String): Boolean {
        // Mock verification logic for Firebase MFA integration
        return code == "123456" 
    }
}
// Block C complete
