package com.shopkeeper.mobileshop.security

import android.content.Context
import android.content.SharedPreferences

object LoginRateLimiter {

    private const val PREFS = "shop_auth_rate_limit"
    private const val KEY_FAILED_COUNT = "login_failed_count"
    private const val KEY_LOCKOUT_UNTIL = "login_lockout_until_ms"

    const val MAX_FAILED_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Checks if user is currently locked out from attempting login.
     * @return Pair of (isLocked: Boolean, secondsRemaining: Long)
     */
    fun checkLockout(context: Context): Pair<Boolean, Long> {
        val prefs = getPrefs(context)
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()

        if (lockoutUntil > now) {
            val remainingSec = (lockoutUntil - now) / 1000L
            return Pair(true, remainingSec)
        }

        // Lockout expired, clear lockout timestamp if needed
        if (lockoutUntil != 0L) {
            prefs.edit()
                .remove(KEY_LOCKOUT_UNTIL)
                .putInt(KEY_FAILED_COUNT, 0)
                .apply()
        }

        return Pair(false, 0L)
    }

    /**
     * Records a failed authentication attempt.
     * If MAX_FAILED_ATTEMPTS reached, triggers a 15-minute lockout.
     * @return The updated number of failed attempts
     */
    fun recordFailure(context: Context): Int {
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_FAILED_COUNT, 0) + 1

        val editor = prefs.edit().putInt(KEY_FAILED_COUNT, currentCount)
        if (currentCount >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            editor.putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
        }
        editor.apply()
        return currentCount
    }

    /**
     * Resets failure counter upon successful login.
     */
    fun reset(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_COUNT, 0)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    /**
     * Gets how many attempts remaining before lockout.
     */
    fun getRemainingAttempts(context: Context): Int {
        val prefs = getPrefs(context)
        val count = prefs.getInt(KEY_FAILED_COUNT, 0)
        return (MAX_FAILED_ATTEMPTS - count).coerceAtLeast(0)
    }
}
