package com.shopkeeper.mobileshop.security

import android.content.Context
import android.content.SharedPreferences

object AppUnlockRateLimiter {

    private const val PREFS = "shop_app_unlock_rate_limit"
    private const val KEY_FAILED_COUNT = "unlock_failed_count"
    private const val KEY_LOCKOUT_UNTIL = "unlock_lockout_until_ms"

    const val MAX_FAILED_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes lockout for app unlock

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Checks if app unlock is currently locked out due to repeated failures.
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

        if (lockoutUntil != 0L) {
            prefs.edit()
                .remove(KEY_LOCKOUT_UNTIL)
                .putInt(KEY_FAILED_COUNT, 0)
                .apply()
        }

        return Pair(false, 0L)
    }

    /**
     * Records a failed app unlock attempt.
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
     * Resets failure counter upon successful unlock.
     */
    fun reset(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_COUNT, 0)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    /**
     * Gets how many unlock attempts remaining before lockout.
     */
    fun getRemainingAttempts(context: Context): Int {
        val prefs = getPrefs(context)
        val count = prefs.getInt(KEY_FAILED_COUNT, 0)
        return (MAX_FAILED_ATTEMPTS - count).coerceAtLeast(0)
    }
}
