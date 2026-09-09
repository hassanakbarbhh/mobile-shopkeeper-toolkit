with open("app/src/main/java/com/shopkeeper/mobileshop/utils/GoogleAuthManager.kt", "r") as f:
    content = f.read()

old_when = """        return when (role) {
            AppMode.SHOP_OWNER -> user.isOwner
            AppMode.SELLER_STAFF, AppMode.REPAIR_TECH -> true // Staff access allowed for signed in user
        }"""
new_when = """        return when (role) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> user.isOwner
            AppMode.SELLER_STAFF, AppMode.REPAIR_TECH, AppMode.BASIC_USER -> true // Staff access allowed for signed in user
        }"""
content = content.replace(old_when, new_when)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/GoogleAuthManager.kt", "w") as f:
    f.write(content)
