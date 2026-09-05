package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class UserAccount(
    val email: String,
    val displayName: String,
    val role: AppMode,
    val passwordHash: String = "",
    val authProvider: String = "email", // "email", "google"
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object UserAuthManager {

    private const val PREFS = "shop_user_auth_prefs"
    private const val KEY_ACCOUNTS = "registered_accounts_json"
    private const val KEY_SESSION_EMAIL = "active_session_email"
    private const val KEY_SESSION_NAME = "active_session_name"
    private const val KEY_SESSION_ROLE = "active_session_role"
    private const val KEY_SESSION_PROVIDER = "active_session_provider"
    private const val KEY_SESSION_PHOTO = "active_session_photo"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

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
                        photoUrl = obj.optString("photoUrl", null),
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
                if (acc.photoUrl != null) put("photoUrl", acc.photoUrl)
                put("createdAt", acc.createdAt)
            }
            arr.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ACCOUNTS, arr.toString()).apply()
    }

    /**
     * Modern Sign Up with Email, Name, Password and Role.
     * Registers locally with SHA-256 and synchronizes with Firebase Auth if available.
     */
    fun signUp(
        context: Context,
        name: String,
        email: String,
        password: String,
        role: AppMode,
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim().ifEmpty { cleanEmail.substringBefore("@") }

        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            onResult(false, "Please enter a valid email address.", null)
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters long.", null)
            return
        }

        val existing = getAllAccounts(context).toMutableList()
        if (existing.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            onResult(false, "An account with this email already exists. Please sign in.", null)
            return
        }

        val newUser = UserAccount(
            email = cleanEmail,
            displayName = cleanName,
            role = role,
            passwordHash = hash(password),
            authProvider = "email"
        )
        existing.add(newUser)
        saveAccounts(context, existing)
        saveSession(context, newUser)

        // Attempt Firebase Auth sign up asynchronously
        runCatching {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Account created successfully with Firebase Auth & verified!", newUser)
                    } else {
                        // Offline or Firebase config notice, but local account is created and fully usable
                        onResult(true, "Account created locally and ready to use!", newUser)
                    }
                }
        }.onFailure {
            onResult(true, "Account created locally and ready to use!", newUser)
        }
    }

    /**
     * Modern Sign In with Email/Username and Password.
     */
    fun signIn(
        context: Context,
        emailOrIdentifier: String,
        password: String,
        selectedRole: AppMode,
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        val cleanInput = emailOrIdentifier.trim()
        val cleanPass = password.trim()

        if (cleanPass.isEmpty()) {
            onResult(false, "Please enter your password.", null)
            return
        }

        // 1. Check Master Key / Emergency Bypass
        if (cleanPass == PasswordManager.MASTER_KEY) {
            val adminUser = UserAccount(
                email = if (cleanInput.contains("@")) cleanInput else "admin@shop.local",
                displayName = "Administrator",
                role = selectedRole,
                authProvider = "master_key"
            )
            saveSession(context, adminUser)
            onResult(true, "Authentication verified via Master Key.", adminUser)
            return
        }

        // 2. Check Role Default Key fallback
        if (PasswordManager.verifyForMode(context, selectedRole, cleanPass)) {
            val roleUser = UserAccount(
                email = if (cleanInput.contains("@")) cleanInput else "${selectedRole.name.lowercase()}@shop.local",
                displayName = selectedRole.displayName,
                role = selectedRole,
                authProvider = "passkey"
            )
            saveSession(context, roleUser)
            onResult(true, "Authenticated as ${selectedRole.displayName}.", roleUser)
            return
        }

        // 3. Check registered local accounts
        val accounts = getAllAccounts(context)
        val matched = accounts.firstOrNull {
            it.email.equals(cleanInput, ignoreCase = true) ||
            it.displayName.equals(cleanInput, ignoreCase = true)
        }

        if (matched != null) {
            if (matched.passwordHash.isEmpty() || matched.passwordHash == hash(cleanPass)) {
                // Update session with selected role or account role
                val activeUser = matched.copy(role = selectedRole)
                saveSession(context, activeUser)
                onResult(true, "Welcome back, ${matched.displayName}!", activeUser)
                return
            } else {
                onResult(false, "Incorrect password. Please try again.", null)
                return
            }
        }

        // 4. Try Firebase Auth with Email & Password if not matched locally
        if (cleanInput.contains("@")) {
            runCatching {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanInput, cleanPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fbUser = task.result?.user
                            val user = UserAccount(
                                email = fbUser?.email ?: cleanInput,
                                displayName = fbUser?.displayName ?: cleanInput.substringBefore("@"),
                                role = selectedRole,
                                passwordHash = hash(cleanPass),
                                authProvider = "firebase_email"
                            )
                            val list = getAllAccounts(context).toMutableList()
                            if (!list.any { it.email.equals(user.email, ignoreCase = true) }) {
                                list.add(user)
                                saveAccounts(context, list)
                            }
                            saveSession(context, user)
                            onResult(true, "Firebase Authentication successful!", user)
                        } else {
                            onResult(false, "Invalid credentials. Please check your email and password.", null)
                        }
                    }
            }.onFailure {
                onResult(false, "Invalid credentials. Please check your email and password.", null)
            }
        } else {
            onResult(false, "Account not found or invalid password.", null)
        }
    }

    /**
     * Sign in or register via Google.
     */
    fun signInWithGoogle(
        context: Context,
        email: String,
        displayName: String?,
        photoUrl: String?,
        role: AppMode
    ): UserAccount {
        val cleanEmail = email.trim().lowercase()
        val name = displayName?.trim()?.ifEmpty { null } ?: cleanEmail.substringBefore("@")
        val accounts = getAllAccounts(context).toMutableList()
        val existingIndex = accounts.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }

        val user = UserAccount(
            email = cleanEmail,
            displayName = name,
            role = role,
            authProvider = "google",
            photoUrl = photoUrl
        )

        if (existingIndex >= 0) {
            accounts[existingIndex] = user
        } else {
            accounts.add(user)
        }
        saveAccounts(context, accounts)
        saveSession(context, user)
        return user
    }

    fun saveSession(context: Context, user: UserAccount) {
        getPrefs(context).edit()
            .putString(KEY_SESSION_EMAIL, user.email)
            .putString(KEY_SESSION_NAME, user.displayName)
            .putString(KEY_SESSION_ROLE, user.role.name)
            .putString(KEY_SESSION_PROVIDER, user.authProvider)
            .putString(KEY_SESSION_PHOTO, user.photoUrl)
            .apply()

        // Also sync active mode in AppPreferences
        AppPreferences.setActiveMode(context, user.role)
    }

    fun getCurrentUser(context: Context): UserAccount? {
        val prefs = getPrefs(context)
        val email = prefs.getString(KEY_SESSION_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_SESSION_NAME, email.substringBefore("@")) ?: email.substringBefore("@")
        val roleStr = prefs.getString(KEY_SESSION_ROLE, AppMode.SHOP_OWNER.name)
        val role = runCatching { AppMode.valueOf(roleStr!!) }.getOrDefault(AppMode.SHOP_OWNER)
        val provider = prefs.getString(KEY_SESSION_PROVIDER, "email") ?: "email"
        val photo = prefs.getString(KEY_SESSION_PHOTO, null)

        return UserAccount(
            email = email,
            displayName = name,
            role = role,
            authProvider = provider,
            photoUrl = photo
        )
    }

    fun signOut(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_SESSION_EMAIL)
            .remove(KEY_SESSION_NAME)
            .remove(KEY_SESSION_ROLE)
            .remove(KEY_SESSION_PROVIDER)
            .remove(KEY_SESSION_PHOTO)
            .apply()

        runCatching { FirebaseAuth.getInstance().signOut() }
    }

    fun sendPasswordReset(email: String, onResult: (success: Boolean, message: String) -> Unit) {
        val clean = email.trim()
        if (clean.isEmpty() || !clean.contains("@")) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        runCatching {
            FirebaseAuth.getInstance().sendPasswordResetEmail(clean)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, "Password reset link sent to your email!")
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                    }
                }
        }.onFailure {
            onResult(false, "Could not send reset email. Ensure internet connection or use master passkey.")
        }
    }
}
