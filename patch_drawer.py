with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    code = f.read()

patch = """        when (mode) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> {
                tvHeaderRoleBadge?.text = if (mode == AppMode.OWNER) "👑 App Owner" else "👑 Shop Owner (Master)"
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = true
                }
            }
"""
code = code.replace("        when (mode) {\n            AppMode.SHOP_OWNER -> {\n                tvHeaderRoleBadge?.text = \"👑 Shop Owner (Master)\"\n                // Owner sees all items\n                for (i in 0 until menu.size()) {\n                    menu.getItem(i).isVisible = true\n                }\n                // Default start\n                // Dashboard is default\n            }", patch)

# Add group_owner visibility check for others
code = code.replace("menu.findItem(R.id.navigation_dashboard)?.isVisible = false", "menu.findItem(R.id.navigation_dashboard)?.isVisible = false\n                menu.findItem(R.id.navigation_access_control)?.isVisible = false")

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.write(code)
