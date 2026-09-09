with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

new_func = """    fun updateLocalRole(context: Context, roleStr: String) {
        val mappedRole = when (roleStr) {
            "OWNER" -> AppMode.OWNER
            "SHOP_OWNER" -> AppMode.SHOP_OWNER
            "RESELLER" -> AppMode.SELLER_STAFF
            "REPAIR_SHOP" -> AppMode.REPAIR_TECH
            else -> AppMode.BASIC_USER
        }
        val user = getCurrentUser(context) ?: return
        val updatedUser = user.copy(role = mappedRole)
        saveSession(context, updatedUser)
    }
}"""
if "updateLocalRole" not in code:
    code = code.rsplit("}", 1)[0] + new_func

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
