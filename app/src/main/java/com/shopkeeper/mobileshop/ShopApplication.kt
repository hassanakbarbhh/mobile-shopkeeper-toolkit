package com.shopkeeper.mobileshop

import android.app.Application
import android.util.Log

import com.shopkeeper.mobileshop.utils.GlobalExceptionHandler
import com.google.firebase.FirebaseApp
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.ThemeManager

class ShopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
        instance = this

        // 1. Crash Shield: Global Uncaught Exception Handler to prevent hard app crashes
        setupCrashShield()

        // 2. Safe Firebase Initialization
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.i(TAG, "Firebase initialized successfully.")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Safe Firebase initialization notice: ${e.message}")
        }

        // 3. Safe Database & Preference Initialization
        try {
            AppDatabase.getDatabase(this)
            CurrencyManager.init(this)
            ThemeManager.applyNightMode(this)
        } catch (e: Throwable) {
            Log.e(TAG, "Component initialization error: ${e.message}")
        }

        // 4. App Lock Security Lifecycle: Lock session when app enters background
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var startedActivityCount = 0

            override fun onActivityStarted(activity: android.app.Activity) {
                startedActivityCount++
            }

            override fun onActivityStopped(activity: android.app.Activity) {
                startedActivityCount--
                if (startedActivityCount <= 0) {
                    startedActivityCount = 0
                    com.shopkeeper.mobileshop.security.AppLockManager.onAppBackgrounded()
                }
            }

            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityResumed(activity: android.app.Activity) {}
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        })
    }

    private fun setupCrashShield() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Shielded Uncaught Exception on [${thread.name}]: ${throwable.message}", throwable)
            try {
                // If it's a fatal UI thread crash, allow fallback
                defaultHandler?.uncaughtException(thread, throwable)
            } catch (_: Throwable) {
                // Prevent cascade crash
            }
        }
    }

    companion object {
        private const val TAG = "ShopApplication"
        lateinit var instance: ShopApplication
            private set
    }
}
