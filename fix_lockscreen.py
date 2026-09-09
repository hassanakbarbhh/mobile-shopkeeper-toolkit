with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    content = f.read()

content = content.replace("AppMode.OWNER, AppMode.SHOP_OWNER -> {", "AppMode.OWNER, AppMode.SHOP_OWNER -> {")

old_repair_role = """                binding.tvRoleHint.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
        }
    }"""
new_repair_role = """                binding.tvRoleHint.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
            AppMode.BASIC_USER -> {
                binding.tvRoleHint.text = "Basic User: Read-only access or waiting for approval"
                binding.btnLoginSubmit.text = "Sign In as Basic User"
            }
            else -> {}
        }
    }"""

if old_repair_role in content:
    content = content.replace(old_repair_role, new_repair_role)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(content)
