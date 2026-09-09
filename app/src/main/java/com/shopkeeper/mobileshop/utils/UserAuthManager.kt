package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.shopkeeper.mobileshop.security.LoginRateLimiter
import com.shopkeeper.mobileshop.security.SecureStorage
import com.shopkeeper.mobileshop.security.SecuritySanitizer
import org.json.JSONArray
import org.json.JSONObject

data class UserAccount(
    val email: String,
    val displayName: String,
    val role: AppMode,
    val passwordHash: String = "",
    val authProvider: String = "email", // "email", "phone_otp", "google"
    val photoUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object UserAuthManager {

    private const val PREFS = "shop_user_auth_secure_prefs"
    private const val KEY_ACCOUNTS = "registered_accounts_json"
    private const val KEY_SESSION_EMAIL = "active_session_email"
    private const val KEY_SESSION_NAME = "active_session_name"
    private const val KEY_SESSION_ROLE = "active_session_role"
    private const val KEY_SESSION_PROVIDER = "active_session_provider"
    private const val KEY_SESSION_PHOTO = "active_session_photo"
    private const val KEY_SESSION_VERIFIED = "active_session_verified"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAllAccounts(context: Context): List<UserAccount> {
        val jsonStr = getPrefs(context).getString(KEY_ACCOUNTS, "[]") ?: "[]"
        val list = mutableListOf<UserAccount>()
        runCatching {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val roleStr = obj.optString("role", AppMode.SHOP_OWNER.name)
                val role = runCatching { AppMode.valueOf(roleStr) }.getOrDefault(AppMode.SHOP_OWNER)
                list.add(
                    UserAccount(
                        email = obj.getString("email"),
                        displayName = obj.optString("displayName", obj.getString("email").substringBefore("@")),
                        role = role,
                        passwordHash = obj.optString("passwordHash", ""),
                        authProvider = obj.optString("authProvider", "email"),
                        photoUrl = if (obj.has("photoUrl")) obj.optString("photoUrl") else null,
                        isVerified = obj.optBoolean("isVerified", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }
        return list
    }

    private fun saveAccounts(context: Context, accounts: List<UserAccount>) {
        val arr = JSONArray()
        for (acc in accounts) {
            val obj = JSONObject().apply {
                put("email", acc.email)
                put("displayName", acc.displayName)
                put("role", acc.role.name)
                put("passwordHash", acc.passwordHash)
                put("authProvider", acc.authProvider)
                put("isVerified", acc.isVerified)
                if (acc.photoUrl != null) put("photoUrl", acc.photoUrl)
                put("createdAt", acc.createdAt)
            }
            arr.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ACCOUNTS, arr.toString()).apply()
    }

    /**
     * Real Sign Up with Email, Name, Password and Role.
     * Enforces strict validation, passwords hashed with salt, and registers with Firebase Auth.
     */
    fun signUp(
        context: Context,
        name: String,
        email: String,
        password: String,
        role: AppMode,
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        val cleanEmail = SecuritySanitizer.sanitize(email).lowercase()
        val cleanName = SecuritySanitizer.sanitize(name).ifEmpty { cleanEmail.substringBefore("@") }

        if (!SecuritySanitizer.isValidEmail(cleanEmail)) {
            onResult(false, "Please enter a valid email address.", null)
            return
        }

        val (isStrong, passMsg) = SecuritySanitizer.validatePasswordStrength(password)
        if (!isStrong) {
            onResult(false, passMsg, null)
            return
        }

        val existing = getAllAccounts(context).toMutableList()
        if (existing.any { acc -> acc.email.equals(cleanEmail, ignoreCase = true) }) {
            onResult(false, "An account with this email already exists. Please sign in.", null)
            return
        }

                // Real Firebase Auth account creation
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fbUser = task.result?.user
                        // Send real email verification link
                        fbUser?.sendEmailVerification()
                        
                        val newUser = UserAccount(
                            email = cleanEmail,
                            displayName = cleanName,
                            role = AppMode.BASIC_USER,
                            passwordHash = SecureStorage.hashPassword(context, password),
                            authProvider = "firebase_email",
                            isVerified = fbUser?.isEmailVerified ?: false
                        )
                        
                        // Add to Firestore
                        fbUser?.let {
                            val userDoc = hashMapOf(
                                "email" to cleanEmail,
                                "displayName" to cleanName,
                                "role" to "BASIC_USER",
                                "approved" to false,
                                "updatedAt" to System.currentTimeMillis()
                            )
                            FirebaseFirestore.getInstance().collection("users").document(it.uid).set(userDoc)
                        }

                        existing.add(newUser)
                        saveAccounts(context, existing)
                        saveSession(context, newUser)
                        LoginRateLimiter.reset(context)
                        onResult(true, "Registration successful!", newUser)
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Registration failed.", null)
                    }
                }
        } catch (e: Throwable) {
            // Fallback for secure offline registration with salted hash
            val newUser = UserAccount(
                email = cleanEmail,
                displayName = cleanName,
                role = role,
                passwordHash = SecureStorage.hashPassword(context, password),
                authProvider = "secure_local"
            )
            existing.add(newUser)
            saveAccounts(context, existing)
            saveSession(context, newUser)
            LoginRateLimiter.reset(context)
            onResult(true, "Account created locally with encrypted credentials.", newUser)
        }
    }

    /**
     * Real Sign In with Email/Username and Password.
     * Enforces rate limiting, salted password verification, and real Firebase authentication.
     */
    fun signIn(
        context: Context,
        emailOrIdentifier: String,
        password: String,
        selectedRole: AppMode,
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        // 1. Enforce Rate Limiting & Brute-Force Bot Protection
        val (isLocked, secondsRemaining) = LoginRateLimiter.checkLockout(context)
        if (isLocked) {
            onResult(
                false,
                "Too many failed login attempts. Account temporarily locked for $secondsRemaining seconds.",
                null
            )
            return
        }

        val cleanInput = SecuritySanitizer.sanitize(emailOrIdentifier).lowercase()
        val cleanPass = password.trim()

        if (cleanPass.isEmpty()) {
            onResult(false, "Please enter your password.", null)
            return
        }

        // 2. First attempt real Firebase Authentication if input is an email
        if (cleanInput.contains("@")) {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanInput, cleanPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fbUser = task.result?.user
                            
                            // Query Firestore for Role
                            fbUser?.let {
                                FirebaseFirestore.getInstance().collection("users").document(it.uid).get()
                                    .addOnSuccessListener { doc ->
                                        val roleStr = if (doc.exists()) doc.getString("role") ?: "BASIC_USER" else "BASIC_USER"
                                        val mappedRole = when (roleStr) {
                                            "OWNER" -> AppMode.OWNER
                                            "SHOP_OWNER" -> AppMode.SHOP_OWNER
                                            "RESELLER" -> AppMode.SELLER_STAFF
                                            "REPAIR_SHOP" -> AppMode.REPAIR_TECH
                                            else -> AppMode.BASIC_USER
                                        }
                                        val user = UserAccount(
                                            email = fbUser.email ?: cleanInput,
                                            displayName = fbUser.displayName ?: cleanInput.substringBefore("@"),
                                            role = mappedRole,
                                            passwordHash = SecureStorage.hashPassword(context, cleanPass),
                                            authProvider = "firebase_email",
                                            isVerified = fbUser.isEmailVerified
                                        )
                                        val list = getAllAccounts(context).toMutableList()
                                        val idx = list.indexOfFirst { acc -> acc.email.equals(user.email, ignoreCase = true) }
                                        if (idx >= 0) list[idx] = user else list.add(user)
                                        saveAccounts(context, list)
                                        saveSession(context, user)
                                        LoginRateLimiter.reset(context)
                                        onResult(true, "Signed in successfully via Firebase Auth!", user)
                                    }
                                    .addOnFailureListener {
                                        // Fallback if offline
                                        val user = UserAccount(
                                            email = fbUser.email ?: cleanInput,
                                            displayName = fbUser.displayName ?: cleanInput.substringBefore("@"),
                                            role = selectedRole,
                                            passwordHash = SecureStorage.hashPassword(context, cleanPass),
                                            authProvider = "firebase_email",
                                            isVerified = fbUser.isEmailVerified
                                        )
                                        val list = getAllAccounts(context).toMutableList()
                                        val idx = list.indexOfFirst { acc -> acc.email.equals(user.email, ignoreCase = true) }
                                        if (idx >= 0) list[idx] = user else list.add(user)
                                        saveAccounts(context, list)
                                        saveSession(context, user)
                                        LoginRateLimiter.reset(context)
                                        onResult(true, "Signed in (Offline).", user)
                                    }
                            }
                        } else {
                            // Check registered local accounts if Firebase failed or user is offline
                            verifyLocalCredentials(context, cleanInput, cleanPass, selectedRole, onResult)
                        }
                    }
                return
            } catch (_: Throwable) {
                // Network unavailable or Firebase uninitialized, proceed to local check
            }
        }

        verifyLocalCredentials(context, cleanInput, cleanPass, selectedRole, onResult)
    }

    private fun verifyLocalCredentials(
        context: Context,
        cleanInput: String,
        cleanPass: String,
        selectedRole: AppMode,
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        // Check registered accounts
        val accounts = getAllAccounts(context)
        val matched = accounts.firstOrNull { acc ->

            acc.email.equals(cleanInput, ignoreCase = true) ||

            acc.displayName.equals(cleanInput, ignoreCase = true)

        }

        if (matched != null) {
            val isCorrect = SecureStorage.verifyPassword(context, cleanPass, matched.passwordHash)
            if (isCorrect) {
                val activeUser = matched.copy(role = selectedRole)
                saveSession(context, activeUser)
                LoginRateLimiter.reset(context)
                onResult(true, "Welcome back, ${matched.displayName}!", activeUser)
                return
            }
        }

        // Check if role-based passkey is configured
        if (PasswordManager.isConfiguredForMode(context, selectedRole) &&
            PasswordManager.verifyForMode(context, selectedRole, cleanPass)
        ) {
            val roleUser = UserAccount(
                email = if (cleanInput.contains("@")) cleanInput else "${selectedRole.name.lowercase()}@shop.local",
                displayName = selectedRole.displayName,
                role = selectedRole,
                authProvider = "passkey"
            )
            saveSession(context, roleUser)
            LoginRateLimiter.reset(context)
            onResult(true, "Authenticated as ${selectedRole.displayName}.", roleUser)
            return
        }

        // Failure - record failed attempt
        val failedCount = LoginRateLimiter.recordFailure(context)
        val remaining = (LoginRateLimiter.MAX_FAILED_ATTEMPTS - failedCount).coerceAtLeast(0)
        val warning = if (remaining > 0) {
            "Invalid credentials. $remaining attempt(s) remaining before lockout."
        } else {
            "Maximum failed attempts reached. Login locked for 15 minutes."
        }
        onResult(false, warning, null)
    }

    /**
     * Authenticate or register using real Firebase Phone / SMS OTP.
     */
    fun signInWithPhoneUser(
        context: Context,
        firebaseUser: FirebaseUser,
        role: AppMode
    ): UserAccount {
        val phone = firebaseUser.phoneNumber ?: "Verified Phone"
        val user = UserAccount(
            email = phone,
            displayName = phone,
            role = role,
            authProvider = "phone_otp",
            isVerified = true
        )

        val list = getAllAccounts(context).toMutableList()
        val idx = list.indexOfFirst { acc -> acc.email == phone }
        if (idx >= 0) list[idx] = user else list.add(user)
        saveAccounts(context, list)

        saveSession(context, user)
        LoginRateLimiter.reset(context)
        return user
    }

    /**
     * Authenticate via real Google Sign-In with verified token.
     */
    fun signInWithGoogle(
        context: Context,
        email: String,
        displayName: String?,
        photoUrl: String?,
        role: AppMode
    ): UserAccount {
        val cleanEmail = SecuritySanitizer.sanitize(email).lowercase()
        val name = SecuritySanitizer.sanitize(displayName).ifEmpty { cleanEmail.substringBefore("@") }
        val accounts = getAllAccounts(context).toMutableList()
        val existingIndex = accounts.indexOfFirst { acc -> acc.email.equals(cleanEmail, ignoreCase = true) }

        val user = UserAccount(
            email = cleanEmail,
            displayName = name,
            role = role,
            authProvider = "google",
            photoUrl = photoUrl,
            isVerified = true
        )

        if (existingIndex >= 0) {
            accounts[existingIndex] = user
        } else {
            accounts.add(user)
        }
        saveAccounts(context, accounts)
        saveSession(context, user)
        LoginRateLimiter.reset(context)
        return user
    }

    fun saveSession(context: Context, user: UserAccount) {
        val encryptedEmail = SecureStorage.encryptString(context, user.email)
        val encryptedName = SecureStorage.encryptString(context, user.displayName)

        getPrefs(context).edit()
            .putString(KEY_SESSION_EMAIL, encryptedEmail)
            .putString(KEY_SESSION_NAME, encryptedName)
            .putString(KEY_SESSION_ROLE, user.role.name)
            .putString(KEY_SESSION_PROVIDER, user.authProvider)
            .putString(KEY_SESSION_PHOTO, user.photoUrl)
            .putBoolean(KEY_SESSION_VERIFIED, user.isVerified)
            .apply()

        AppPreferences.setActiveMode(context, user.role)
    }

    fun getCurrentUser(context: Context): UserAccount? {
        val prefs = getPrefs(context)
        val rawEmail = prefs.getString(KEY_SESSION_EMAIL, null) ?: return null
        val email = SecureStorage.decryptString(context, rawEmail).ifEmpty { rawEmail }
        if (email.isBlank()) return null

        val rawName = prefs.getString(KEY_SESSION_NAME, null)
        val name = if (rawName != null) SecureStorage.decryptString(context, rawName).ifEmpty { rawName } else email.substringBefore("@")
        val roleStr = prefs.getString(KEY_SESSION_ROLE, AppMode.SHOP_OWNER.name)
        val role = runCatching { AppMode.valueOf(roleStr!!) }.getOrDefault(AppMode.SHOP_OWNER)
        val provider = prefs.getString(KEY_SESSION_PROVIDER, "email") ?: "email"
        val photo = prefs.getString(KEY_SESSION_PHOTO, null)
        val isVerified = prefs.getBoolean(KEY_SESSION_VERIFIED, false)

        return UserAccount(
            email = email,
            displayName = name,
            role = role,
            authProvider = provider,
            photoUrl = photo,
            isVerified = isVerified
        )
    }

    fun signOut(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(KEY_SESSION_EMAIL)
        editor.remove(KEY_SESSION_NAME)
        editor.remove(KEY_SESSION_ROLE)
        editor.remove(KEY_SESSION_PROVIDER)
        editor.remove(KEY_SESSION_PHOTO)
        editor.remove(KEY_SESSION_VERIFIED)
        editor.apply()
        runCatching { FirebaseAuth.getInstance().signOut() }
    }

    fun sendPasswordReset(email: String, onResult: (success: Boolean, message: String) -> Unit) {
        val clean = SecuritySanitizer.sanitize(email).lowercase()
        if (!SecuritySanitizer.isValidEmail(clean)) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(clean)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password reset link sent to $clean!")
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                    }
                }
        } catch (e: Throwable) {
            onResult(false, "Could not send reset email: ${e.message}")
        }
    }
    fun updateLocalRole(context: Context, roleStr: String) {
        val mappedRole = when (roleStr) {
            "OWNER" -> AppMode.OWNER
            "SHOP_OWNER" -> AppMode.SHOP_OWNER
            "RESELLER" -> AppMode.SELLER_STAFF
            "REPAIR_SHOP" -> AppMode.REPAIR_TECH
            else -> AppMode.BASIC_USER
        }
        val user = getCurrentUser(context) ?: return
        val updatedUser = user.copy(role = mappedRole)
        saveSession(context, updatedUser)
    }
}
