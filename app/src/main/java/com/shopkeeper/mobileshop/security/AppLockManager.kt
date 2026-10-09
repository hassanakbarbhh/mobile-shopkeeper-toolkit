package com.shopkeeper.mobileshop.security

/**
 * Manages in-memory app unlock state across app lifecycle events.
 * When the app goes to the background or the user triggers an explicit lock,
 * the session state is locked and re-authentication is required.
 */
object AppLockManager {
    @Volatile
    private var unlocked: Boolean = false

    fun isAppUnlocked(): Boolean = unlocked

    fun setUnlocked(isUnlocked: Boolean) {
        unlocked = isUnlocked
    }

    fun onAppBackgrounded() {
        unlocked = false
    }

    fun lockNow() {
        unlocked = false
    }
}
