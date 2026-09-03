package com.shopkeeper.mobileshop.utils

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
class PasswordManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PasswordManager.resetToDefault(context)
    }

    @Test
    fun testMasterKeyHassanisgreatAlwaysValid() {
        // User requested: login interface key Hassanisgreat must unlock
        assertEquals("Default password constant must be Hassanisgreat",
            "Hassanisgreat", PasswordManager.DEFAULT_PASSWORD)
        assertTrue("PasswordManager verify must accept 'Hassanisgreat'",
            PasswordManager.verify(context, "Hassanisgreat"))
        assertTrue("PasswordManager verify must accept lowercase 'hassanisgreat'",
            PasswordManager.verify(context, "hassanisgreat"))
    }

    @Test
    fun testMasterKeyWorksEvenWhenPasswordChanged() {
        // Change password to something else
        val changed = PasswordManager.changePassword(context, "Hassanisgreat", "NewSecretPass123")
        assertTrue("Password change should succeed", changed)

        // New password should work
        assertTrue("New password should work",
            PasswordManager.verify(context, "NewSecretPass123"))

        // Master key 'Hassanisgreat' MUST STILL WORK unconditionally
        assertTrue("Master key 'Hassanisgreat' must work even after password change",
            PasswordManager.verify(context, "Hassanisgreat"))
    }

    @Test
    fun testWrongPasswordFails() {
        assertFalse("Wrong password must fail",
            PasswordManager.verify(context, "WrongPass999"))
        assertFalse("Blank password must fail",
            PasswordManager.verify(context, "   "))
    }

    @Test
    fun testResetToDefault() {
        PasswordManager.changePassword(context, "Hassanisgreat", "Temporary123")
        assertTrue(PasswordManager.verify(context, "Temporary123"))

        PasswordManager.resetToDefault(context)
        assertTrue("After reset, Hassanisgreat works",
            PasswordManager.verify(context, "Hassanisgreat"))
    }
}
