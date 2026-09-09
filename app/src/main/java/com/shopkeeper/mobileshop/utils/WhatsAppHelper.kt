package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {
    fun sendTextToWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            // Ensure country code if missing? The user might have it or not.
            // If they just have local format, WhatsApp API might require country code. 
            // We just send exactly what we have, cleaned up.
            
            val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Set package explicitly to avoid chooser if we only want WhatsApp
                // setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed or error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
