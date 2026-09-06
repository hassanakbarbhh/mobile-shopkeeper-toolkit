package com.shopkeeper.mobileshop.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

data class AuthenticatedUser(
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isOwner: Boolean
)

object GoogleAuthManager {
    private const val PREFS = "shop_google_auth_prefs"
    private const val KEY_OWNER_EMAIL = "authorized_owner_email"
    private const val KEY_USER_EMAIL = "logged_in_user_email"
    private const val KEY_USER_NAME = "logged_in_user_name"
    private const val KEY_USER_PHOTO = "logged_in_user_photo"
    private const val KEY_REQUIRE_GOOGLE_LOGIN = "require_google_login"
    private const val KEY_STAFF_EMAILS = "authorized_staff_emails"

    // Default owner email requested by user
    const val DEFAULT_OWNER_EMAIL = "hassanakbarbhh@gmail.com"

    // Official Web Client ID from Firebase google-services.json for ID Token generation
    const val FIREBASE_WEB_CLIENT_ID = "762164247372-k4lhffr6qfk4a9ho6shmdc30fmhjrs61.apps.googleusercontent.com"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else FIREBASE_WEB_CLIENT_ID
        } catch (_: Throwable) {
            FIREBASE_WEB_CLIENT_ID
        }

        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()

        if (webClientId.isNotBlank()) {
            builder.requestIdToken(webClientId)
        }

        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getOwnerEmail(context: Context): String {
        return getPrefs(context).getString(KEY_OWNER_EMAIL, DEFAULT_OWNER_EMAIL) ?: DEFAULT_OWNER_EMAIL
    }

    fun getAuthorizedOwnerEmail(context: Context): String = getOwnerEmail(context)

    fun isOwnerAccessGranted(context: Context): Boolean {
        val user = getAuthenticatedUser(context)
        return user != null && user.isOwner
    }

    fun setOwnerEmail(context: Context, email: String) {
        getPrefs(context).edit().putString(KEY_OWNER_EMAIL, email.trim().lowercase()).apply()
    }

    fun isOwnerEmail(context: Context, email: String): Boolean {
        val owner = getOwnerEmail(context).trim().lowercase()
        return email.trim().lowercase() == owner
    }

    fun isRequireGoogleLogin(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REQUIRE_GOOGLE_LOGIN, true)
    }

    fun setRequireGoogleLogin(context: Context, required: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_REQUIRE_GOOGLE_LOGIN, required).apply()
    }

    fun saveAuthenticatedUser(
        context: Context,
        email: String,
        displayName: String?,
        photoUrl: String?
    ) {
        val cleanEmail = email.trim().lowercase()
        val name = displayName ?: cleanEmail.substringBefore("@")
        getPrefs(context).edit()
            .putString(KEY_USER_EMAIL, cleanEmail)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHOTO, photoUrl)
            .apply()
    }

    fun getAuthenticatedUser(context: Context): AuthenticatedUser? {
        val lastGoogleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastGoogleAccount != null && !lastGoogleAccount.email.isNullOrBlank()) {
            val email = lastGoogleAccount.email!!.trim().lowercase()
            val name = lastGoogleAccount.displayName ?: email.substringBefore("@")
            val photo = lastGoogleAccount.photoUrl?.toString()
            saveAuthenticatedUser(context, email, name, photo)
            return AuthenticatedUser(
                email = email,
                displayName = name,
                photoUrl = photo,
                isOwner = isOwnerEmail(context, email)
            )
        }

        val prefs = getPrefs(context)
        val savedEmail = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val savedName = prefs.getString(KEY_USER_NAME, savedEmail.substringBefore("@")) ?: savedEmail
        val photo = prefs.getString(KEY_USER_PHOTO, null)
        return AuthenticatedUser(
            email = savedEmail,
            displayName = savedName,
            photoUrl = photo,
            isOwner = isOwnerEmail(context, savedEmail)
        )
    }

    fun canAccessRole(context: Context, role: AppMode, user: AuthenticatedUser?): Boolean {
        if (user == null) return false
        return when (role) {
            AppMode.SHOP_OWNER -> user.isOwner
            AppMode.SELLER_STAFF, AppMode.REPAIR_TECH -> true // Staff access allowed for signed in user
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        try {
            getGoogleSignInClient(context).signOut().addOnCompleteListener {
                clearSavedUser(context)
                try {
                    FirebaseAuth.getInstance().signOut()
                } catch (_: Throwable) {}
                onComplete()
            }
        } catch (e: Exception) {
            clearSavedUser(context)
            onComplete()
        }
    }

    fun clearSavedUser(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PHOTO)
            .apply()
    }

    fun isFirebaseInitialized(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }

    fun tryFirebaseAuth(idToken: String?, onDone: (Boolean) -> Unit) {
        if (idToken.isNullOrBlank()) {
            onDone(false)
            return
        }
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    onDone(task.isSuccessful)
                }
        } catch (e: Throwable) {
            onDone(false)
        }
    }
}
