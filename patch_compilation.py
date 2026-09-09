with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Fix the deprecated remove methods and ensure syntax is valid at the end
code = code.replace(".remove(", ".removeAt(")

# For SharedPreferences, the method to remove keys is remove(String). removeAt(Int) is for lists. 
# Revert the shared prefs remove
code = code.replace("edit()\n            .removeAt(KEY_SESSION_EMAIL)", "edit()\n            .remove(KEY_SESSION_EMAIL)")
code = code.replace(".removeAt(KEY_SESSION_NAME)", ".remove(KEY_SESSION_NAME)")
code = code.replace(".removeAt(KEY_SESSION_ROLE)", ".remove(KEY_SESSION_ROLE)")
code = code.replace(".removeAt(KEY_SESSION_PROVIDER)", ".remove(KEY_SESSION_PROVIDER)")
code = code.replace(".removeAt(KEY_SESSION_PHOTO)", ".remove(KEY_SESSION_PHOTO)")
code = code.replace(".removeAt(KEY_SESSION_VERIFIED)", ".remove(KEY_SESSION_VERIFIED)")

# In the catch block of signIn
code = code.replace("""        } catch (e: Throwable) {
            // Fallback for secure offline registration with salted hash""", """                }
        } catch (e: Throwable) {
            // Fallback for secure offline registration with salted hash""")

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
