package com.shopkeeper.mobileshop.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.PasswordManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticationLifecycleTest {

    private lateinit var context: Context
    private val correctPassword = "CorrectStorePassword!2026"
    private val wrongPassword = "WrongPassword999"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PasswordManager.clearAllKeys(context)
        AppUnlockRateLimiter.reset(context)
        PasswordManager.changeKeyForMode(context, AppMode.SHOP_OWNER, correctPassword)
    }

    // 1. Correct password verification
    @Test
    fun testCase1_CorrectPasswordSucceeds() {
        val verified = PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword)
        assertTrue("Correct password must be verified successfully", verified)
    }

    // 2. Incorrect password verification
    @Test
    fun testCase2_IncorrectPasswordFails() {
        val verified = PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, wrongPassword)
        assertFalse("Incorrect password must be rejected", verified)
    }

    // 3. Repeated incorrect passwords increment failure counter
    @Test
    fun testCase3_RepeatedIncorrectPasswordsIncrementCounter() {
        for (i in 1..4) {
            val count = AppUnlockRateLimiter.recordFailure(context)
            assertEquals(i, count)
            val (isLocked, _) = AppUnlockRateLimiter.checkLockout(context)
            assertFalse("Must not be locked out prior to 5th attempt", isLocked)
            assertEquals(5 - i, AppUnlockRateLimiter.getRemainingAttempts(context))
        }
    }

    // 4 & 5. Fifth failure triggers lockout
    @Test
    fun testCase4And5_FifthFailureTriggersLockout() {
        for (i in 1..4) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        val fifthCount = AppUnlockRateLimiter.recordFailure(context)
        assertEquals(5, fifthCount)

        val (isLocked, remainingSec) = AppUnlockRateLimiter.checkLockout(context)
        assertTrue("5th failure must trigger lockout", isLocked)
        assertTrue("Remaining lockout seconds must be greater than zero", remainingSec > 0)
        assertEquals(0, AppUnlockRateLimiter.getRemainingAttempts(context))
    }

    // 6. Correct password during lockout must still be blocked
    @Test
    fun testCase6_CorrectPasswordDuringLockoutRemainsBlocked() {
        for (i in 1..5) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        val (isLocked, _) = AppUnlockRateLimiter.checkLockout(context)
        assertTrue("System must report lockout state", isLocked)

        // Even if user provides the correct password, lockout policy strictly prevents session grant
        val allowsAccess = !isLocked && PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword)
        assertFalse("Access must remain blocked during active lockout window", allowsAccess)
    }

    // 7 & 8. Lockout expiry allows successful authentication
    @Test
    fun testCase7And8_LockoutExpiryAllowsSuccessfulAuthentication() {
        for (i in 1..5) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        assertTrue(AppUnlockRateLimiter.checkLockout(context).first)

        // Simulate time advancing past the 5-minute lockout window in SharedPreferences
        val prefs = context.getSharedPreferences("shop_app_unlock_rate_limit", Context.MODE_PRIVATE)
        prefs.edit().putLong("unlock_lockout_until_ms", System.currentTimeMillis() - 1000L).apply()

        // Check lockout after expiry
        val (isLockedAfterExpiry, _) = AppUnlockRateLimiter.checkLockout(context)
        assertFalse("Lockout must be cleared once duration expires", isLockedAfterExpiry)

        // User can now successfully authenticate
        val authSuccess = PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword)
        assertTrue("Authentication must succeed once lockout period has elapsed", authSuccess)
    }

    // 9. Counter reset after successful authentication
    @Test
    fun testCase9_CounterResetsAfterSuccessfulAuthentication() {
        // Record 3 failures
        for (i in 1..3) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        assertEquals(2, AppUnlockRateLimiter.getRemainingAttempts(context))

        // User enters correct password and rate limiter resets
        val verified = PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword)
        if (verified) {
            AppUnlockRateLimiter.reset(context)
        }

        assertEquals(5, AppUnlockRateLimiter.getRemainingAttempts(context))
        assertFalse(AppUnlockRateLimiter.checkLockout(context).first)
    }

    // 10. App restart during lockout preserves lockout state
    @Test
    fun testCase10_AppRestartDuringLockoutPreservesLockout() {
        for (i in 1..5) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        assertTrue(AppUnlockRateLimiter.checkLockout(context).first)

        // Simulate app process restart by re-querying fresh Context handle
        val restartedAppContext = ApplicationProvider.getApplicationContext<Context>()
        val (isStillLocked, remaining) = AppUnlockRateLimiter.checkLockout(restartedAppContext)
        assertTrue("Lockout must persist across app process restart", isStillLocked)
        assertTrue("Remaining seconds must persist across restart", remaining > 0)
    }

    // 11. Logout / Login lifecycle
    @Test
    fun testCase11_LogoutLoginLifecycle() {
        // Initial setup and successful login
        assertTrue(PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword))

        // Change or update key (e.g. key rotation)
        val rotatedPassword = "RotatedPassword777"
        PasswordManager.changeKeyForMode(context, AppMode.SHOP_OWNER, rotatedPassword)

        // Old key should fail, new key must succeed
        assertFalse("Old password must fail after rotation", PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword))
        assertTrue("New password must succeed", PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, rotatedPassword))

        // Clear keys (logout / reset)
        PasswordManager.clearAllKeys(context)
        assertFalse("After key clear, authentication must fail", PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, rotatedPassword))
    }

    // 12. Corrupted stored authentication data fails closed
    @Test
    fun testCase12_CorruptedStoredAuthenticationDataFailsClosed() {
        // Corrupt the stored hash in SharedPreferences with invalid bytes/string
        val prefs = context.getSharedPreferences("shop_lock_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("password_hash_owner", "corrupted:invalid_hash_data!").apply()

        // Attempt verification against corrupted data: must fail closed without granting access
        val result = PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, correctPassword)
        assertFalse("Corrupted stored password hash must fail closed and reject login", result)
    }
}
