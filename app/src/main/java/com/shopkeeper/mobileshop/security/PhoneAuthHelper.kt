package com.shopkeeper.mobileshop.security

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

object PhoneAuthHelper {

    interface OtpCallback {
        fun onOtpSent(verificationId: String)
        fun onAutoVerified(user: FirebaseUser)
        fun onError(message: String)
    }

    /**
     * Triggers real SMS OTP sending via Firebase Phone Authentication.
     */
    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        callback: OtpCallback
    ) {
        val auth = FirebaseAuth.getInstance()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant auto-retrieval on supported devices/SIMs
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful && task.result?.user != null) {
                            callback.onAutoVerified(task.result!!.user!!)
                        } else {
                            callback.onError(task.exception?.localizedMessage ?: "Auto-verification failed")
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                callback.onError(e.localizedMessage ?: "Phone verification failed. Please check the number format.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                callback.onOtpSent(verificationId)
            }
        }

        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Throwable) {
            callback.onError("Unable to send OTP: ${e.message}")
        }
    }

    /**
     * Verifies user-entered 6-digit SMS OTP code against the Firebase verification ID.
     */
    fun verifyOtp(
        activity: Activity,
        verificationId: String,
        code: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code.trim())
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful && task.result?.user != null) {
                        onSuccess(task.result!!.user!!)
                    } else {
                        val msg = task.exception?.localizedMessage ?: "Invalid verification code. Please try again."
                        onError(msg)
                    }
                }
        } catch (e: Throwable) {
            onError("Verification error: ${e.message}")
        }
    }
}
