    // Compatibility aliases
    fun signInWithGoogle(context: Context, email: String, displayName: String, photoUrl: String?, role: AppMode): UserAccount {
        val cleanEmail = email.lowercase()
        val cleanName = displayName
        val user = UserAccount(email = cleanEmail, displayName = cleanName, role = role, authProvider = "google", photoUrl = photoUrl, isVerified = true)
        saveSession(context, user)
        return user
    }

    fun getCurrentUser(context: Context): UserAccount? {
        return getCurrentSession(context)
    }

    fun signInWithPhoneUser(context: Context, fbUser: FirebaseUser, role: AppMode): UserAccount {
        val phoneNumber = fbUser.phoneNumber ?: "Unknown Phone"
        val user = UserAccount(email = phoneNumber, displayName = phoneNumber, role = role, authProvider = "phone_otp", isVerified = true)
        saveSession(context, user)
        return user
    }

    fun sendPasswordReset(context: Context, email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.widget.Toast.makeText(context, "Password reset link sent to $email", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Failed to send reset email: ${task.exception?.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
    }

    fun updateLocalRole(context: Context, user: UserAccount, newRole: AppMode) {
        val updated = user.copy(role = newRole)
        saveSession(context, updated)
        val all = getAllAccounts(context).map { if (it.email == user.email) updated else it }
        saveAccounts(context, all)
    }
}
