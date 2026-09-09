with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

import re

old_signout = """    fun signOut(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_SESSION_EMAIL)
            .remove(KEY_SESSION_NAME)
            .remove(KEY_SESSION_ROLE)
            .remove(KEY_SESSION_PROVIDER)
            .remove(KEY_SESSION_PHOTO)
            .remove(KEY_SESSION_VERIFIED)
            .apply()
        runCatching { FirebaseAuth.getInstance().signOut() }
    }"""

new_signout = """    fun signOut(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(KEY_SESSION_EMAIL)
        editor.remove(KEY_SESSION_NAME)
        editor.remove(KEY_SESSION_ROLE)
        editor.remove(KEY_SESSION_PROVIDER)
        editor.remove(KEY_SESSION_PHOTO)
        editor.remove(KEY_SESSION_VERIFIED)
        editor.apply()
        runCatching { FirebaseAuth.getInstance().signOut() }
    }"""

code = code.replace(old_signout, new_signout)

# Fix trailing brackets.
code = code.rstrip()
while code.endswith("}"):
    code = code[:-1].rstrip()
code = code + "\n    }\n}\n"

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
