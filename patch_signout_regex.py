import re
with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Just replace .remove(...) with val editor = ... editor.remove(...) etc
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

code = re.sub(r'    fun signOut\(context: Context\) \{.*?\n    \}', new_signout, code, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
