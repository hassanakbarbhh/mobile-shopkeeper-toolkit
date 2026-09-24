package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.net.Uri
import com.shopkeeper.mobileshop.data.db.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupManager {
    private const val DB_NAME = AppDatabase.DATABASE_NAME

    fun backupDatabase(context: Context, destUri: Uri): Boolean {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                // Check if old name exists as fallback
                val fallbackFile = context.getDatabasePath("shopkeeper_db")
                if (!fallbackFile.exists()) return false
            }

            val sourceFile = if (dbFile.exists()) dbFile else context.getDatabasePath("shopkeeper_db")

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
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
            val dbFile = context.getDatabasePath(DB_NAME)
            
            // Temporary file for safe restore
            val tempFile = File(context.cacheDir, "${DB_NAME}_temp")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // If successful copy to temp, replace original database file
            if (tempFile.exists() && tempFile.length() > 0) {
                // Close active Room connections before replacing
                AppDatabase.closeDatabase()
                
                tempFile.copyTo(dbFile, overwrite = true)
                tempFile.delete()

                // Remove lingering journal or wal files from previous session
                val walFile = File(dbFile.path + "-wal")
                if (walFile.exists()) walFile.delete()
                val shmFile = File(dbFile.path + "-shm")
                if (shmFile.exists()) shmFile.delete()

                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
