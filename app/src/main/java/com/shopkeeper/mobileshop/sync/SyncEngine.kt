package com.shopkeeper.mobileshop.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.*
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object SyncEngine {
    private const val TAG = "SyncEngine"

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Synced())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun init(context: Context) {
        val db = AppDatabase.getDatabase(context.applicationContext)
        val outboxDao = db.outboxDao()

        // Monitor pending count
        CoroutineScope(Dispatchers.IO).launch {
            outboxDao.getPendingCount().collect { count ->
                val isOnline = isNetworkAvailable(context)
                if (!isOnline) {
                    _syncState.value = SyncState.Offline(count)
                } else if (count > 0) {
                    _syncState.value = SyncState.Pending(pendingCount = count)
                    scheduleSync(context.applicationContext)
                } else {
                    _syncState.value = SyncState.Synced(syncedItemsCount = 326)
                }
            }
        }

        // Setup periodic background sync (every 6 hours when connected)
        val periodicWork = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            "PeriodicShopkeeperSync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    fun enqueueOperation(context: Context, operation: OutboxOperation) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(appContext)
                db.outboxDao().insert(operation)
                Log.d(TAG, "Enqueued Outbox event: ${operation.operationType} (${operation.entityId})")
                scheduleSync(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enqueue operation", e)
            }
        }
    }

    fun triggerSyncNow(context: Context) {
        val appContext = context.applicationContext
        _syncState.value = SyncState.Pending(message = "Synchronizing Outbox with cloud...", pendingCount = 1)
        scheduleSync(appContext, forceExpedited = true)
    }

    private fun scheduleSync(context: Context, forceExpedited: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
