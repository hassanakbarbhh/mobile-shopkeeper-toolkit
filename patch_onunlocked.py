with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    code = f.read()

patch = """    private fun onUnlocked(mode: AppMode) {
        if (mode == AppMode.BASIC_USER) {
            val intent = Intent(this, PendingApprovalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        AppPreferences.setActiveMode(this, mode)"""

code = code.replace("    private fun onUnlocked(mode: AppMode) {\n        AppPreferences.setActiveMode(this, mode)", patch)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(code)
