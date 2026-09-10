import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

# Fix duplicate imports
content = content.replace("import com.google.firebase.auth.FirebaseAuth\nimport com.google.firebase.firestore.FirebaseFirestore\nimport com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException\nimport android.util.Log\n", "", 1)

# Fix Unresolved reference 'clearLogs'
content = content.replace("GlobalExceptionHandler.clearLogs(requireContext())", "GlobalExceptionHandler.clearLogs(requireContext())\n                    Toast.makeText(requireContext(), \"Logs cleared\", Toast.LENGTH_SHORT).show()")

# Fix Unresolved reference 'LoginActivity'
content = content.replace("com.shopkeeper.mobileshop.ui.auth.LoginActivity", "com.shopkeeper.mobileshop.ui.auth.LockScreenActivity")

# The exception said Unresolved reference 'flags' because intent was declared implicitly with a property assignment but Intent() without arguments returns a type without flags setter in kotlin unless specified as val intent = Intent(...)

content = content.replace("val intent = Intent(requireContext(), com.shopkeeper.mobileshop.ui.auth.LockScreenActivity::class.java)", "val intent = Intent(requireContext(), com.shopkeeper.mobileshop.ui.auth.LockScreenActivity::class.java)")
# Actually the issue is Kotlin 1.7+ Intent constructor
content = content.replace("intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK", "intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)")

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
