    fun signUp(
        context: Context,
        name: String,
        email: String,
        password: String,
        role: AppMode,
        shopName: String = "",
        shopCode: String = "",
        onResult: (success: Boolean, message: String, user: UserAccount?) -> Unit
    ) {
        val cleanEmail = SecuritySanitizer.sanitize(email).lowercase()
        val cleanName = SecuritySanitizer.sanitize(name)
        val existing = getSavedAccounts(context).toMutableList()

        if (existing.any { it.email == cleanEmail }) {
            onResult(false, "An account with this email already exists.", null)
            return
        }

        try {
            // Attempt REAL Firebase creation
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fbUser = task.result?.user
                        
                        // Send real email verification link
                        fbUser?.sendEmailVerification()
                        
                        val newUser = UserAccount(
                            email = cleanEmail,
                            displayName = cleanName,
                            role = role,
                            passwordHash = SecureStorage.hashPassword(context, password),
                            authProvider = "firebase_email",
                            isVerified = fbUser?.isEmailVerified ?: false
                        )
                        
                        // Add to Firestore
                        fbUser?.let {
                            val userDoc = hashMapOf<String, Any>(
                                "email" to cleanEmail,
                                "displayName" to cleanName,
                                "role" to role.name,
                                "approved" to (role == AppMode.SHOP_OWNER), // Owner is auto-approved, seller needs approval
                                "shopName" to shopName,
                                "shopCode" to (if (role == AppMode.SHOP_OWNER) java.util.UUID.randomUUID().toString().substring(0, 8).uppercase() else shopCode),
                                "updatedAt" to System.currentTimeMillis()
                            )
                            FirebaseFirestore.getInstance().collection("users").document(it.uid).set(userDoc)
                        }

                        existing.add(newUser)
                        saveAccounts(context, existing)
                        
                        // DO NOT save session on signup. Must verify email first.
                        FirebaseAuth.getInstance().signOut()
                        LoginRateLimiter.reset(context)
                        onResult(true, "Registration successful! A verification link has been sent to your email. Please verify before logging in.", newUser)
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
