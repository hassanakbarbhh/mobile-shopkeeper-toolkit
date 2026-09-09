def replace_in_file(path, old, new):
    with open(path, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(path, "w") as f:
        f.write(content)

# LockScreenActivity
lock_old = """            AppMode.REPAIR_TECH -> {
                binding.tvRoleHint.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
        }"""
lock_new = """            AppMode.REPAIR_TECH -> {
                binding.tvRoleHint.text = "🔧 Repair Tech: Access to repairs, spare parts & inventory only"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
            }
            AppMode.BASIC_USER -> {
                binding.tvRoleHint.text = "Basic User"
                binding.btnLoginSubmit.text = "Sign In"
            }
            else -> {}
        }"""
replace_in_file("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", lock_old, lock_new)

# SettingsFragment
set_old = """                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyRepair)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
                }
            }"""
set_new = """                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyRepair)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
                }
                else -> {}
            }"""
replace_in_file("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", set_old, set_new)

