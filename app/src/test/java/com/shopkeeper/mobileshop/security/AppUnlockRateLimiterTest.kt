package com.shopkeeper.mobileshop.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppUnlockRateLimiterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppUnlockRateLimiter.reset(context)
    }

    @Test
    fun testInitialUnlockStateNotLocked() {
        val (isLocked, _) = AppUnlockRateLimiter.checkLockout(context)
        assertFalse(isLocked)
        assertEquals(5, AppUnlockRateLimiter.getRemainingAttempts(context))
    }

    @Test
    fun testUnlockLockoutTriggeredAfterMaxFailures() {
        for (i in 1..4) {
            AppUnlockRateLimiter.recordFailure(context)
            val (isLocked, _) = AppUnlockRateLimiter.checkLockout(context)
            assertFalse("Should not be locked after $i attempts", isLocked)
        }

        // 5th attempt triggers lockout
        AppUnlockRateLimiter.recordFailure(context)
        val (isLocked, remainingSec) = AppUnlockRateLimiter.checkLockout(context)
        assertTrue("Should be locked out after 5 failures", isLocked)
        assertTrue("Remaining seconds should be positive", remainingSec > 0)
    }

    @Test
    fun testUnlockResetClearsLockout() {
        for (i in 1..5) {
            AppUnlockRateLimiter.recordFailure(context)
        }
        assertTrue(AppUnlockRateLimiter.checkLockout(context).first)

        AppUnlockRateLimiter.reset(context)
        assertFalse(AppUnlockRateLimiter.checkLockout(context).first)
        assertEquals(5, AppUnlockRateLimiter.getRemainingAttempts(context))
    }
}
