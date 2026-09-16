                    val shopName = binding.etShopName.text?.toString()?.trim().orEmpty()
                    val shopNumber = binding.etShopNumber.text?.toString()?.trim().orEmpty()
                    val shopAddress = binding.etShopAddress.text?.toString()?.trim().orEmpty()
                    val shopCode = binding.etShopCode.text?.toString()?.trim().orEmpty()

                    UserAuthManager.signInWithGoogle(
                        context = this,
                        idToken = account.idToken,
                        email = email,
                        displayName = name,
                        photoUrl = photoUrl,
                        role = currentRole,
                        shopName = shopName,
                        shopNumber = shopNumber,
                        shopAddress = shopAddress,
                        shopCode = shopCode
                    ) { success, message, user ->
                        if (success && user != null) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            onUnlocked(user.role)
                        } else {
                            showError(message)
                        }
                    }
