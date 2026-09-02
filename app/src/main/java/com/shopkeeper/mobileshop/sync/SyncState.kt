package com.shopkeeper.mobileshop.sync

sealed class SyncState {
    data class Synced(
        val lastSyncTimestamp: Long,
        val commitSha: String = "HEAD",
        val syncedItemsCount: Int = 0
    ) : SyncState()

    data class Pending(
        val message: String = "Syncing with GitHub repository...",
        val pendingCount: Int = 0
    ) : SyncState()

    data class Error(
        val errorMessage: String,
        val lastAttemptTimestamp: Long = System.currentTimeMillis(),
        val canRetry: Boolean = true
    ) : SyncState()
}
