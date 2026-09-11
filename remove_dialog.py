import re
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    content = f.read()

content = re.sub(r'    private fun showGoogleAccountEntryDialog\(\) \{.*?\n    \}\n', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(content)
