import re

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Add Firestore import if missing
if "FirebaseFirestore" not in code:
    code = code.replace("import com.google.firebase.auth.FirebaseUser", "import com.google.firebase.auth.FirebaseUser\nimport com.google.firebase.firestore.FirebaseFirestore")

# Update signUp method to include Firestore
new_signup = """        // Real Firebase Auth account creation
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
        }"""

code = re.sub(r'// Real Firebase Auth account creation.*?\n\s+try \{.*?\}\s+\}', new_signup, code, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
