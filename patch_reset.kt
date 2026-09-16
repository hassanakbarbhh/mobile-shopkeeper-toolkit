    fun sendPasswordReset(context: Context, email: String, onResult: (Boolean, String) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset link sent to $email.")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                }
            }
    }
