import re

# Fix LockScreenActivity
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    content = f.read()

content = re.sub(
    r'AppMode\.REPAIR_TECH -> \{\s*binding\.tvRoleHint\.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"\s*binding\.btnLoginSubmit\.text = "Sign In as Repair Tech"\s*\}\s*\}',
    """AppMode.REPAIR_TECH -> {
                binding.tvRoleHint.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
            AppMode.BASIC_USER -> {
                binding.tvRoleHint.text = "Basic User"
                binding.btnLoginSubmit.text = "Sign In"
            }
            else -> {}
        }""",
    content
)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(content)


# Fix SettingsFragment
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

content = re.sub(
    r'AppMode\.REPAIR_TECH -> \{\s*dBinding\.toggleKeyRole\.check\(R\.id\.btnKeyRepair\)\s*dBinding\.tvKeyRoleSubtitle\.text = "Modifying security key for Repair Tech"\s*\}\s*\}',
    """AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyRepair)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
                }
                else -> {}
            }""",
    content
)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)

