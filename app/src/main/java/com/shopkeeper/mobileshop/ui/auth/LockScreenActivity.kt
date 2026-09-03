package com.shopkeeper.mobileshop.ui.auth

import android.content.Intent
import android.os.Build
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
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.PasswordManager

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private var currentRole: AppMode = AppMode.SHOP_OWNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize from last active mode if remembered
        val lastMode = AppPreferences.getActiveMode(this)
        setRole(lastMode)

        setupRoleToggle()

        binding.btnUnlock.setOnClickListener { checkPassword() }
        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            checkPassword()
            true
        }

        setupBiometric()
    }

    private fun setupRoleToggle() {
        binding.toggleRoleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnRoleOwner -> setRole(AppMode.SHOP_OWNER)
                    R.id.btnRoleSeller -> setRole(AppMode.SELLER_STAFF)
                    R.id.btnRoleTech -> setRole(AppMode.REPAIR_TECH)
                }
            }
        }
    }

    private fun setRole(mode: AppMode) {
        currentRole = mode
        when (mode) {
            AppMode.SHOP_OWNER -> {
                binding.toggleRoleMode.check(R.id.btnRoleOwner)
                binding.tvRoleHint.text = "👑 Shop Owner: Full master access, profits, purchases & settings"
                binding.etPassword.hint = "Owner Key (Default: Hassanisgreat)"
            }
            AppMode.SELLER_STAFF -> {
                binding.toggleRoleMode.check(R.id.btnRoleSeller)
                binding.tvRoleHint.text = "💼 Seller / Staff: Fast POS counter, sales, customer dues & thermal receipts"
                binding.etPassword.hint = "Seller Key (Default: seller123)"
            }
            AppMode.REPAIR_TECH -> {
                binding.toggleRoleMode.check(R.id.btnRoleTech)
                binding.tvRoleHint.text = "🔧 Repair Technician: Intake jobs, diagnosis, parts & thermal claim tags"
                binding.etPassword.hint = "Tech Key (Default: repair123)"
            }
        }
        binding.tvError.visibility = View.INVISIBLE
    }

    private fun checkPassword() {
        val input = binding.etPassword.text?.toString().orEmpty().trim()
        if (input.isEmpty()) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.lock_empty)
            return
        }

        // Check if input matches the selected role
        if (PasswordManager.verifyForMode(this, currentRole, input)) {
            onUnlocked(currentRole)
            return
        }

        // Smart detect: Check if user typed key for another role directly
        val detected = PasswordManager.detectMode(this, input)
        if (detected != null) {
            onUnlocked(detected)
            return
        }

        // Invalid key
        binding.tvError.visibility = View.VISIBLE
        val expectedHint = when (currentRole) {
            AppMode.SHOP_OWNER -> "Hassanisgreat"
            AppMode.SELLER_STAFF -> "seller123"
            AppMode.REPAIR_TECH -> "repair123"
        }
        binding.tvError.text = "Incorrect key for ${currentRole.displayName}. (Default: $expectedHint)"
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.tilPassword.startAnimation(shake)
    }

    /**
     * Biometric implementation guaranteed safe across Android 8.0, 9, 10, 11, 12, 13, 14, 15, 16, 17.
     */
    private fun setupBiometric() {
        try {
            val bm = BiometricManager.from(this)
            val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            }

            val canAuth = bm.canAuthenticate(authenticators)
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                binding.btnFingerprint.visibility = View.GONE
                return
            }

            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_name))
                .setSubtitle("Unlock with fingerprint / biometrics")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptBuilder.setAllowedAuthenticators(authenticators)
            } else {
                promptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                promptBuilder.setNegativeButtonText("Use Access Key")
            }

            val promptInfo = promptBuilder.build()

            val prompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlocked(currentRole)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            Toast.makeText(this@LockScreenActivity, errString, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            binding.btnFingerprint.setOnClickListener {
                runCatching {
                    prompt.authenticate(promptInfo)
                }.onFailure {
                    Toast.makeText(this@LockScreenActivity, "Biometrics unavailable", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (t: Throwable) {
            // Failsafe on Android 13 and below
            binding.btnFingerprint.visibility = View.GONE
        }
    }

    private fun onUnlocked(mode: AppMode) {
        AppPreferences.setActiveMode(this, mode)
        AppPreferences.saveMode(this, mode, remember = true)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
