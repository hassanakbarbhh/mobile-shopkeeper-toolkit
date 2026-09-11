with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "binding.btnManageGoogleAuth" in line or "binding.btnSetupMfa" in line or "binding.btnCheckFirebaseAuth" in line:
        skip = True
    
    if skip and line.strip() == "}":
        skip = False
        continue
    
    if not skip:
        if "binding.tvSettingsGoogleAccount" not in line and "binding.tvSettingsGoogleSubtitle" not in line:
            new_lines.append(line)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.writelines(new_lines)
