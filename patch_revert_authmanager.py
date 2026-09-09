import re

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Fix the remove issue
code = code.replace(".removeAt(KEY_SESSION_EMAIL)", ".remove(KEY_SESSION_EMAIL)")
code = code.replace(".removeAt(KEY_SESSION_NAME)", ".remove(KEY_SESSION_NAME)")
code = code.replace(".removeAt(KEY_SESSION_ROLE)", ".remove(KEY_SESSION_ROLE)")
code = code.replace(".removeAt(KEY_SESSION_PROVIDER)", ".remove(KEY_SESSION_PROVIDER)")
code = code.replace(".removeAt(KEY_SESSION_PHOTO)", ".remove(KEY_SESSION_PHOTO)")
code = code.replace(".removeAt(KEY_SESSION_VERIFIED)", ".remove(KEY_SESSION_VERIFIED)")

# Fix missing "getAllAccounts" and "saveAccounts" - they probably got deleted accidentally by the previous script.
# Let's see if they are in the file.
if "fun getAllAccounts" not in code:
    print("getAllAccounts missing!")
    
