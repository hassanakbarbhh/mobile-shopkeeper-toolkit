package com.shopkeeper.mobileshop.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.shopkeeper.mobileshop.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles seamless backup and restore of the Room Database.
 * Allows user to export to local storage or Google Drive via Android's Storage Access Framework.
 */
object DatabaseBackupManager {

    /**
     * Launch intent to save DB Backup.
     */
    fun startBackup(activity: Activity, requestCode: Int) {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "MobileShop_Backup_$dateStr.db")
        }
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Launch intent to pick a DB Backup file to restore.
     */
    fun startRestore(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Allow all since mime types can be restrictive across Google Drive
        }
        activity.startActivityForResult(intent, requestCode)
    }

    suspend fun performBackup(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Ensure data is completely flushed from memory/WAL to the actual .db file
            AppDatabase.getDatabase(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("shopkeeper_db")
            if (!dbFile.exists()) return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun performRestore(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Close the database to release locks
            AppDatabase.getDatabase(context).close()

            val dbFile = context.getDatabasePath("shopkeeper_db")
            
            // Delete WAL and SHM if they exist to prevent corruption
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // Copy over the new DB
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
