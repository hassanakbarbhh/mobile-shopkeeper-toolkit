import re
with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Let's just fix the block manually from line 114 to 171
# It's better to just write a simple regex to fix the catch block.
fixed_catch = """                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Registration failed.", null)
                    }
                }
        } catch (e: Throwable) {"""

code = re.sub(r'                    \} else \{\n                        onResult\(false, task\.exception\?\.localizedMessage \?\: "Registration failed\.", null\)\n                    \}\n                \}\n        \}\n                \}\n        \} catch \(e: Throwable\) \{', fixed_catch, code)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
