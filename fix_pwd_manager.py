with open("app/src/main/java/com/shopkeeper/mobileshop/utils/PasswordManager.kt", "r") as f:
    content = f.read()

old_when = """        return when (mode) {
            AppMode.SHOP_OWNER -> KEY_OWNER_HASH
            AppMode.SELLER_STAFF -> KEY_SELLER_HASH
            AppMode.REPAIR_TECH -> KEY_REPAIR_HASH
        }"""
new_when = """        return when (mode) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> KEY_OWNER_HASH
            AppMode.SELLER_STAFF -> KEY_SELLER_HASH
            AppMode.REPAIR_TECH -> KEY_REPAIR_HASH
            else -> ""
        }"""
if old_when in content:
    content = content.replace(old_when, new_when)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/PasswordManager.kt", "w") as f:
    f.write(content)
