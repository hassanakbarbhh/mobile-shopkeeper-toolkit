package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppUtils {
    fun sendInvoice(context: Context, phoneNumber: String, invoiceText: String) {
        try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
            val encodedMessage = URLEncoder.encode(invoiceText, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
            // Fallback to general share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, invoiceText)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
        }
    }
}
