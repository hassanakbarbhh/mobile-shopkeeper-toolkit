package com.shopkeeper.mobileshop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.shopkeeper.mobileshop.MainActivity
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.ActivityLockScreenBinding
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.PasswordManager

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUnlock.setOnClickListener { checkPassword() }
        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            checkPassword()
            true
        }

        setupBiometric()
    }

    private fun checkPassword() {
        val input = binding.etPassword.text?.toString().orEmpty()
        if (PasswordManager.verify(this, input)) {
            onUnlocked()
        } else {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = "Incorrect password. Default: Hassanisgreat"
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            binding.tilPassword.startAnimation(shake)
        }
    }

    private fun setupBiometric() {
        val bm = BiometricManager.from(this)
        val canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnFingerprint.visibility = View.GONE
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle("Unlock with fingerprint / biometrics")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@LockScreenActivity, errString, Toast.LENGTH_SHORT).show()
                    }
                }
            })

        binding.btnFingerprint.setOnClickListener { prompt.authenticate(promptInfo) }
    }

    private fun onUnlocked() {
        val mode = AppPreferences.getMode(this)
        if (mode != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, RoleSelectActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
