import re

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

new_signin = """                FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanInput, cleanPass)
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
                                        val idx = list.indexOfFirst { it.email.equals(user.email, ignoreCase = true) }
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
                                        val idx = list.indexOfFirst { it.email.equals(user.email, ignoreCase = true) }
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
                    }"""

code = re.sub(r'                FirebaseAuth\.getInstance\(\)\.signInWithEmailAndPassword.*?verifyLocalCredentials\(context, cleanInput, cleanPass, selectedRole, onResult\)\s+\}\s+\}', new_signin, code, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
