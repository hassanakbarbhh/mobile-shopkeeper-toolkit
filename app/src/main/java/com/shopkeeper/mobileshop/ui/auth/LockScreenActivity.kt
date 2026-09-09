package com.shopkeeper.mobileshop.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.MainActivity
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.ActivityLockScreenBinding
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.GoogleAuthManager
import com.shopkeeper.mobileshop.utils.UserAccount
import com.shopkeeper.mobileshop.utils.UserAuthManager

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private var currentRole: AppMode = AppMode.SHOP_OWNER
    private var isSignUpMode: Boolean = false
    private var isOtpMode: Boolean = false
    private var phoneVerificationId: String? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null && !account.email.isNullOrBlank()) {
                    val email = account.email!!.trim().lowercase()
                    val name = account.displayName ?: email.substringBefore("@")
                    val photoUrl = account.photoUrl?.toString()

                    val user = UserAuthManager.signInWithGoogle(
                        context = this,
                        email = email,
                        displayName = name,
                        photoUrl = photoUrl,
                        role = currentRole
                    )

                    // Also link to Firebase if ID token present
                    GoogleAuthManager.tryFirebaseAuth(account.idToken) { _ -> }

                    Toast.makeText(this, "Signed in as ${user.displayName}", Toast.LENGTH_SHORT).show()
                    onUnlocked(currentRole)
                }
            } catch (e: ApiException) {
                // If Play Services is not available or local developer preview, offer email entry fallback
                showGoogleAccountEntryDialog()
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize from last active mode
        val lastMode = AppPreferences.getActiveMode(this)
        setRole(lastMode)

        setupAuthModeToggle()
        setupRoleToggle()
        setupGoogleAuth()
        setupEmailAuth()
        setupOtpAuth()
        setupBiometric()

        checkExistingSession()
    }

    override fun onResume() {
        super.onResume()
        checkExistingSession()
        checkRateLimitStatus()
    }

    private fun checkRateLimitStatus() {
        val (isLocked, secondsRemaining) = com.shopkeeper.mobileshop.security.LoginRateLimiter.checkLockout(this)
        if (isLocked) {
            binding.btnLoginSubmit.isEnabled = false
            showError("Login locked: $secondsRemaining seconds remaining due to repeated failed attempts.")
        } else {
            binding.btnLoginSubmit.isEnabled = true
        }
    }

    private fun checkExistingSession() {
        val session = UserAuthManager.getCurrentUser(this)
        if (session != null) {
            binding.cardActiveSession.visibility = View.VISIBLE
            binding.tvSessionName.text = session.displayName
            binding.tvSessionEmail.text = session.email
            binding.tvSessionAvatar.text = session.displayName.firstOrNull()?.uppercase() ?: "U"
            binding.tvSessionRoleBadge.text = when (session.role) {
                AppMode.SHOP_OWNER -> "👑 Shop Owner"
                AppMode.SELLER_STAFF -> "💼 Counter Seller"
                AppMode.REPAIR_TECH -> "🔧 Repair Tech"
                else -> "User"
            }

            binding.btnSessionEnter.setOnClickListener {
                onUnlocked(session.role)
            }

            binding.btnSessionSignOut.setOnClickListener {
                UserAuthManager.signOut(this)
                binding.cardActiveSession.visibility = View.GONE
                binding.layoutAuthForms.visibility = View.VISIBLE
            }
        } else {
            binding.cardActiveSession.visibility = View.GONE
            binding.layoutAuthForms.visibility = View.VISIBLE
        }
    }

    private fun setupAuthModeToggle() {
        binding.toggleAuthMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnTabSignIn -> {
                        isSignUpMode = false
                        isOtpMode = false
                        binding.layoutSignInForm.visibility = View.VISIBLE
                        binding.layoutOtpForm.visibility = View.GONE
                        binding.layoutSignUpForm.visibility = View.GONE
                    }
                    R.id.btnTabOtp -> {
                        isSignUpMode = false
                        isOtpMode = true
                        binding.layoutSignInForm.visibility = View.GONE
                        binding.layoutOtpForm.visibility = View.VISIBLE
                        binding.layoutSignUpForm.visibility = View.GONE
                    }
                    R.id.btnTabSignUp -> {
                        isSignUpMode = true
                        isOtpMode = false
                        binding.layoutSignInForm.visibility = View.GONE
                        binding.layoutOtpForm.visibility = View.GONE
                        binding.layoutSignUpForm.visibility = View.VISIBLE
                    }
                }
                binding.tvAuthError.visibility = View.GONE
            }
        }
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
            AppMode.OWNER, AppMode.SHOP_OWNER -> {
                binding.toggleRoleMode.check(R.id.btnRoleOwner)
                binding.tvRoleHint.text = "👑 Shop Owner: Full master access, profits, purchases & settings"
                binding.btnLoginSubmit.text = "Sign In as Shop Owner"
            }
            AppMode.SELLER_STAFF -> {
                binding.toggleRoleMode.check(R.id.btnRoleSeller)
                binding.tvRoleHint.text = "💼 Seller / Staff: Fast counter checkout, sales & receipts"
                binding.btnLoginSubmit.text = "Sign In as Seller / Staff"
            }
            AppMode.REPAIR_TECH -> {
                binding.toggleRoleMode.check(R.id.btnRoleTech)
                binding.tvRoleHint.text = "🔧 Repair Technician: Intake jobs, diagnosis & parts tracking"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
            AppMode.BASIC_USER -> {
                binding.tvRoleHint.text = "Basic User"
                binding.btnLoginSubmit.text = "Sign In"
            }
            else -> {}
        }
        binding.tvAuthError.visibility = View.GONE
    }

    private fun setupGoogleAuth() {
        binding.btnGoogleSignIn.setOnClickListener {
            launchGoogleSignIn()
        }
    }

    private fun launchGoogleSignIn() {
        try {
            val client = GoogleAuthManager.getGoogleSignInClient(this)
            googleSignInLauncher.launch(client.signInIntent)
        } catch (e: Exception) {
            showGoogleAccountEntryDialog()
        }
    }

    private fun showGoogleAccountEntryDialog() {
        val input = EditText(this).apply {
            hint = "Enter your Google / Gmail address"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(40, 30, 40, 30)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Google Account Sign-In")
            .setMessage("Enter your Gmail address to connect your account:")
            .setView(input)
            .setPositiveButton("Continue") { _, _ ->
                val email = input.text.toString().trim()
                if (email.contains("@")) {
                    val user = UserAuthManager.signInWithGoogle(
                        context = this,
                        email = email,
                        displayName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                        photoUrl = null,
                        role = currentRole
                    )
                    Toast.makeText(this, "Welcome, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                    onUnlocked(currentRole)
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupEmailAuth() {
        // Sign In Submit
        binding.btnLoginSubmit.setOnClickListener {
            handleSignIn()
        }

        binding.etLoginPassword.setOnEditorActionListener { _, _, _ ->
            handleSignIn()
            true
        }

        // Sign Up Submit
        binding.btnRegisterSubmit.setOnClickListener {
            handleSignUp()
        }

        binding.etRegisterConfirm.setOnEditorActionListener { _, _, _ ->
            handleSignUp()
            true
        }

        // Forgot Password
        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun setupOtpAuth() {
        binding.btnSendOtp.setOnClickListener {
            val rawPhone = binding.etPhoneNumber.text?.toString()?.trim().orEmpty()
            if (!com.shopkeeper.mobileshop.security.SecuritySanitizer.isValidPhone(rawPhone)) {
                showError("Please enter a valid phone number with country code (e.g. +14155552671 or +923001234567).")
                return@setOnClickListener
            }

            binding.btnSendOtp.isEnabled = false
            binding.btnSendOtp.text = "Sending OTP..."
            binding.tvAuthError.visibility = View.GONE

            com.shopkeeper.mobileshop.security.PhoneAuthHelper.sendOtp(
                activity = this,
                phoneNumber = rawPhone,
                callback = object : com.shopkeeper.mobileshop.security.PhoneAuthHelper.OtpCallback {
                    override fun onOtpSent(verificationId: String) {
                        phoneVerificationId = verificationId
                        binding.btnSendOtp.isEnabled = true
                        binding.btnSendOtp.text = "Resend Code"
                        binding.layoutOtpCodeInput.visibility = View.VISIBLE
                        binding.tvOtpCodePrompt.text = "Code sent to $rawPhone. Enter 6-digit code below:"
                        Toast.makeText(this@LockScreenActivity, "SMS verification code sent!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAutoVerified(user: com.google.firebase.auth.FirebaseUser) {
                        val session = UserAuthManager.signInWithPhoneUser(this@LockScreenActivity, user, currentRole)
                        Toast.makeText(this@LockScreenActivity, "Auto-verified: Welcome ${session.displayName}!", Toast.LENGTH_SHORT).show()
                        onUnlocked(currentRole)
                    }

                    override fun onError(message: String) {
                        binding.btnSendOtp.isEnabled = true
                        binding.btnSendOtp.text = "Send 6-Digit SMS Code"
                        showError(message)
                    }
                }
            )
        }

        binding.btnVerifyOtp.setOnClickListener {
            val vId = phoneVerificationId
            if (vId.isNullOrEmpty()) {
                showError("Please request an SMS verification code first.")
                return@setOnClickListener
            }

            val code = binding.etOtpCode.text?.toString()?.trim().orEmpty()
            if (code.length != 6) {
                showError("Please enter the complete 6-digit verification code.")
                return@setOnClickListener
            }

            binding.btnVerifyOtp.isEnabled = false
            binding.btnVerifyOtp.text = "Verifying..."

            com.shopkeeper.mobileshop.security.PhoneAuthHelper.verifyOtp(
                activity = this,
                verificationId = vId,
                code = code,
                onSuccess = { fbUser ->
                    val user = UserAuthManager.signInWithPhoneUser(this, fbUser, currentRole)
                    Toast.makeText(this, "Phone verified successfully! Welcome, ${user.displayName}", Toast.LENGTH_SHORT).show()
                    onUnlocked(currentRole)
                },
                onError = { err ->
                    binding.btnVerifyOtp.isEnabled = true
                    binding.btnVerifyOtp.text = "Verify Code & Sign In"
                    showError(err)
                }
            )
        }
    }

    private fun handleSignIn() {
        val emailOrId = binding.etLoginEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etLoginPassword.text?.toString()?.trim().orEmpty()

        if (pass.isEmpty()) {
            showError("Please enter your password.")
            return
        }

        UserAuthManager.signIn(
            context = this,
            emailOrIdentifier = emailOrId,
            password = pass,
            selectedRole = currentRole
        ) { success, message, user ->
            if (success && user != null) {
                binding.tvAuthError.visibility = View.GONE
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                onUnlocked(user.role)
            } else {
                showError(message)
            }
        }
    }

    private fun handleSignUp() {
        val name = binding.etRegisterName.text?.toString()?.trim().orEmpty()
        val email = binding.etRegisterEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etRegisterPassword.text?.toString()?.trim().orEmpty()
        val confirm = binding.etRegisterConfirm.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            showError("Please enter your full name.")
            return
        }
        if (email.isEmpty() || !email.contains("@")) {
            showError("Please enter a valid email address.")
            return
        }
        if (pass.length < 6) {
            showError("Password must be at least 6 characters.")
            return
        }
        if (pass != confirm) {
            showError("Passwords do not match.")
            return
        }

        UserAuthManager.signUp(
            context = this,
            name = name,
            email = email,
            password = pass,
            role = currentRole
        ) { success, message, user ->
            if (success && user != null) {
                binding.tvAuthError.visibility = View.GONE
                Toast.makeText(this, "Account created! Welcome, ${user.displayName}", Toast.LENGTH_SHORT).show()
                onUnlocked(user.role)
            } else {
                showError(message)
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter your registered email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(binding.etLoginEmail.text?.toString()?.trim().orEmpty())
            setPadding(40, 30, 40, 30)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email address to receive password reset instructions:")
            .setView(input)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    UserAuthManager.sendPasswordReset(email) { ok, msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter an email address.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        binding.tvAuthError.text = message
        binding.tvAuthError.visibility = View.VISIBLE
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        when {
            isSignUpMode -> binding.layoutSignUpForm.startAnimation(shake)
            isOtpMode -> binding.layoutOtpForm.startAnimation(shake)
            else -> binding.layoutSignInForm.startAnimation(shake)
        }
    }

    private fun setupBiometric() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnFingerprint.visibility = View.GONE
            return
        }

        binding.btnFingerprint.visibility = View.VISIBLE
        binding.btnFingerprint.setOnClickListener {
            val executor = ContextCompat.getMainExecutor(this)
            val prompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlocked(currentRole)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            Toast.makeText(this@LockScreenActivity, errString, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Shopkeeper")
                .setSubtitle("Authenticate using your device biometric")
                .setNegativeButtonText("Cancel")
                .build()

            prompt.authenticate(promptInfo)
        }
    }

    private fun onUnlocked(mode: AppMode) {
        if (mode == AppMode.BASIC_USER) {
            val intent = Intent(this, PendingApprovalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        AppPreferences.setActiveMode(this, mode)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_ROLE", mode.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
