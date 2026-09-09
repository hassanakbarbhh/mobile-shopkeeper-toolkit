with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

old_repair_tech = """                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyRepair)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
                }
            }
        }"""
new_repair_tech = """                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyRepair)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
                }
                else -> {}
            }
        }"""
if old_repair_tech in content:
    content = content.replace(old_repair_tech, new_repair_tech)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
