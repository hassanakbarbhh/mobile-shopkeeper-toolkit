    fun signInWithGoogle(
        context: Context,
        idToken: String?,
        email: String,
        displayName: String,
        photoUrl: String?,
        role: AppMode,
        shopName: String = "",
        shopNumber: String = "",
        shopAddress: String = "",
        shopCode: String = "",
        onResult: (Boolean, String, UserAccount?) -> Unit
    ) {
        val cleanEmail = email.lowercase()
        val cleanName = displayName
        
        // Ensure Firebase Auth is signed in first
        GoogleAuthManager.tryFirebaseAuth(idToken) { success ->
            val fbUser = FirebaseAuth.getInstance().currentUser
            if (!success || fbUser == null) {
                onResult(false, "Failed to authenticate with Firebase.", null)
                return@tryFirebaseAuth
            }
            
            // Check if user exists in Firestore
            FirebaseFirestore.getInstance().collection("users").document(fbUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // User exists, handle login
                        val isApproved = doc.getBoolean("approved") ?: false
                        val roleStr = doc.getString("role") ?: role.name
                        val actualRole = runCatching { AppMode.valueOf(roleStr) }.getOrDefault(role)
                        
                        if (actualRole == AppMode.SELLER_STAFF && !isApproved) {
                            FirebaseAuth.getInstance().signOut()
                            onResult(false, "Your account is pending approval from the shop owner.", null)
                        } else {
                            val user = UserAccount(
                                email = cleanEmail,
                                displayName = doc.getString("displayName") ?: cleanName,
                                role = actualRole,
                                authProvider = "google",
                                photoUrl = photoUrl,
                                isVerified = true
                            )
                            saveSession(context, user)
                            onResult(true, "Login Successful", user)
                        }
                    } else {
                        // New user, handle signup validations
                        if ((role == AppMode.SHOP_OWNER || role == AppMode.REPAIR_TECH) && shopName.isEmpty()) {
                            FirebaseAuth.getInstance().signOut()
                            onResult(false, "New Account: Please switch to the Sign Up tab, fill in your Shop details, and try Google Sign-In again.", null)
                            return@addOnSuccessListener
                        }
                        if (role == AppMode.SELLER_STAFF && shopCode.isEmpty()) {
                            FirebaseAuth.getInstance().signOut()
                            onResult(false, "New Account: Please switch to the Sign Up tab, provide the Shop Owner Link Code, and try Google Sign-In again.", null)
                            return@addOnSuccessListener
                        }
                        
                        // Proceed to create new user in Firestore
                        val isApproved = (role == AppMode.SHOP_OWNER)
                        val actualShopCode = if (role == AppMode.SHOP_OWNER) java.util.UUID.randomUUID().toString().substring(0, 8).uppercase() else shopCode
                        
                        val userDoc = hashMapOf<String, Any>(
                            "email" to cleanEmail,
                            "displayName" to cleanName,
                            "role" to role.name,
                            "approved" to isApproved,
                            "shopName" to shopName,
                            "shopNumber" to shopNumber,
                            "shopAddress" to shopAddress,
                            "shopCode" to actualShopCode,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        
                        FirebaseFirestore.getInstance().collection("users").document(fbUser.uid).set(userDoc)
                            .addOnSuccessListener {
                                if (role == AppMode.SELLER_STAFF && !isApproved) {
                                    FirebaseAuth.getInstance().signOut()
                                    onResult(false, "Registration successful. Your account is pending approval from the shop owner.", null)
                                } else {
                                    val newUser = UserAccount(
                                        email = cleanEmail,
                                        displayName = cleanName,
                                        role = role,
                                        authProvider = "google",
                                        photoUrl = photoUrl,
                                        isVerified = true
                                    )
                                    saveSession(context, newUser)
                                    onResult(true, "Registration successful!", newUser)
                                }
                            }
                            .addOnFailureListener { e ->
                                FirebaseAuth.getInstance().signOut()
                                onResult(false, "Failed to create account: ${e.localizedMessage}", null)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    FirebaseAuth.getInstance().signOut()
                    onResult(false, "Failed to check account status: ${e.localizedMessage}", null)
                }
        }
    }
