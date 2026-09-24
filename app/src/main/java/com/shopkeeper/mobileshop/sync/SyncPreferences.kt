package com.shopkeeper.mobileshop.sync

import android.content.Context

object SyncPreferences {
    private const val PREFS = "shop_sync_state_prefs"
    private const val KEY_LAST_SYNC_TIME = "sync_last_time"
    private const val KEY_LAST_SYNC_CURSOR = "sync_last_cursor"
    private const val KEY_DOWNLOADED_COUNT = "sync_downloaded_count"
    private const val KEY_LAST_ERROR = "sync_last_error"

    fun getLastSyncTimestamp(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun setLastSyncTimestamp(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .apply()
    }

    fun getLastSyncCursor(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC_CURSOR, 0L)
    }

    fun setLastSyncCursor(context: Context, cursor: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_CURSOR, cursor)
            .apply()
    }

    fun getDownloadedCount(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_DOWNLOADED_COUNT, 0)
    }

    fun incrementDownloadedCount(context: Context, count: Int) {
        val current = getDownloadedCount(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DOWNLOADED_COUNT, current + count)
            .apply()
    }

    fun setLastError(context: Context, error: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_ERROR, error)
            .apply()
    }

    fun getLastError(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_ERROR, null)
    }
}
