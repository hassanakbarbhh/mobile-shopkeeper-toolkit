with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    code = f.read()

patch = """
            AppMode.BASIC_USER -> {
                tvHeaderRoleBadge?.text = "Pending Approval"
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = false
                }
                menu.findItem(R.id.navigation_lock_app)?.isVisible = true
            }
        }
"""
code = code.replace("        }", patch, 1)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.write(code)
