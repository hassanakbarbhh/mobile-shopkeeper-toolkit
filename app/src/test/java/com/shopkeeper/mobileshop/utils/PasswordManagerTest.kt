package com.shopkeeper.mobileshop.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PasswordManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PasswordManager.clearAllKeys(context)
    }

    @Test
    fun testUnconfiguredRoleFailsVerification() {
        assertFalse(
            "Unconfigured role without an established key must reject access",
            PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, "AnyPassword123")
        )
    }

    @Test
    fun testSetKeyAndVerifySaltedHash() {
        val changed = PasswordManager.changeKeyForMode(context, AppMode.SHOP_OWNER, "SecureOwnerKey999")
        assertTrue("Password key change must succeed", changed)
        assertTrue("Role must now be configured", PasswordManager.isConfiguredForMode(context, AppMode.SHOP_OWNER))

        assertTrue(
            "Correct password must verify successfully",
            PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, "SecureOwnerKey999")
        )

        assertFalse(
            "Wrong password must fail",
            PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, "WrongPassword")
        )
        assertFalse(
            "Blank password must fail",
            PasswordManager.verifyForMode(context, AppMode.SHOP_OWNER, "   ")
        )
    }

    @Test
    fun testResetClearsKeys() {
        PasswordManager.changeKeyForMode(context, AppMode.SELLER_STAFF, "SellerPass123")
        assertTrue(PasswordManager.verifyForMode(context, AppMode.SELLER_STAFF, "SellerPass123"))

        PasswordManager.clearAllKeys(context)
        assertFalse(
            "After clearing keys, old password must not work",
            PasswordManager.verifyForMode(context, AppMode.SELLER_STAFF, "SellerPass123")
        )
    }
}
