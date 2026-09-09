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
class LoginRateLimiterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        LoginRateLimiter.reset(context)
    }

    @Test
    fun testInitialStateNotLocked() {
        val (isLocked, _) = LoginRateLimiter.checkLockout(context)
        assertFalse(isLocked)
        assertEquals(5, LoginRateLimiter.getRemainingAttempts(context))
    }

    @Test
    fun testLockoutTriggeredAfterMaxFailures() {
        for (i in 1..4) {
            LoginRateLimiter.recordFailure(context)
            val (isLocked, _) = LoginRateLimiter.checkLockout(context)
            assertFalse("Should not be locked after $i attempts", isLocked)
        }

        // 5th attempt triggers lockout
        LoginRateLimiter.recordFailure(context)
        val (isLocked, remainingSec) = LoginRateLimiter.checkLockout(context)
        assertTrue("Should be locked out after 5 failures", isLocked)
        assertTrue("Remaining seconds should be positive", remainingSec > 0)
    }

    @Test
    fun testResetClearsLockout() {
        for (i in 1..5) {
            LoginRateLimiter.recordFailure(context)
        }
        assertTrue(LoginRateLimiter.checkLockout(context).first)

        LoginRateLimiter.reset(context)
        assertFalse(LoginRateLimiter.checkLockout(context).first)
        assertEquals(5, LoginRateLimiter.getRemainingAttempts(context))
    }
}
