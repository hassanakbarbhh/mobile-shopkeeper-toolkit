package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.Date

class GlobalExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            val logFile = File(context.filesDir, "crash_logs.txt")
            val writer = FileWriter(logFile, true)
            val printWriter = PrintWriter(writer)
            printWriter.println("--- CRASH at ${Date()} ---")
            exception.printStackTrace(printWriter)
            printWriter.println("-----------------------------------")
            printWriter.close()
        } catch (e: Exception) {
            Log.e("GlobalExceptionHandler", "Failed to write crash log", e)
        }
        defaultHandler?.uncaughtException(thread, exception)
    }

    companion object {
        fun readLogs(context: Context): String {
            val logFile = File(context.filesDir, "crash_logs.txt")
            return if (logFile.exists()) logFile.readText() else "No crash logs found."
        }
    }
}
