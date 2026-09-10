package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class DatabaseBackupManager {
    companion object {
        suspend fun performBackup(context: Context, uri: Uri): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val json = JSONObject()
                    val out = context.contentResolver.openOutputStream(uri)
                    val writer = OutputStreamWriter(out)
                    writer.write(json.toString(2))
                    writer.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        suspend fun performRestore(context: Context, uri: Uri): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val inp = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inp))
                    val jsonStr = reader.readText()
                    reader.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
    }
}
