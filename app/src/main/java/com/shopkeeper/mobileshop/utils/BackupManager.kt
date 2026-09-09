package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    fun backupDatabase(context: Context, destUri: Uri): Boolean {
        try {
            val dbFile = context.getDatabasePath("shopkeeper_db")
            if (!dbFile.exists()) return false

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun restoreDatabase(context: Context, sourceUri: Uri): Boolean {
        try {
            val dbFile = context.getDatabasePath("shopkeeper_db")
            
            // Temporary file for safe restore
            val tempFile = File(context.cacheDir, "shopkeeper_db_temp")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // If successful copy to temp, replace original
            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile.copyTo(dbFile, overwrite = true)
                tempFile.delete()
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
