import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    content = f.read()

# Modify setupBiometric
new_setup = """    private fun setupBiometric() {
        val user = FirebaseAuth.getInstance().currentUser
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        
        // Hide fingerprint if no user is signed in to cloud OR device has no biometric
        if (user == null || canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnFingerprint.visibility = View.GONE
            return
        }

        binding.btnFingerprint.visibility = View.VISIBLE"""
        
content = re.sub(r'    private fun setupBiometric\(\) \{\n        val biometricManager = BiometricManager\.from\(this\)\n        val canAuth = biometricManager\.canAuthenticate\(\n            BiometricManager\.Authenticators\.BIOMETRIC_STRONG or\n            BiometricManager\.Authenticators\.BIOMETRIC_WEAK\n        \)\n        if \(canAuth != BiometricManager\.BIOMETRIC_SUCCESS\) \{\n            binding\.btnFingerprint\.visibility = View\.GONE\n            return\n        \}\n\n        binding\.btnFingerprint\.visibility = View\.VISIBLE', new_setup, content, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(content)
