with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "binding.btnManageGoogleAuth.setOnClickListener {" in line:
        skip = True
    elif "binding.btnSetupMfa.setOnClickListener {" in line:
        skip = True
    elif "binding.btnCheckFirebaseAuth.setOnClickListener {" in line:
        skip = True
    
    if skip and line.strip() == "}":
        skip = False
        continue

    if not skip:
        # Also remove references to tvSettingsGoogleSubtitle
        if "binding.tvSettingsGoogleSubtitle.text" not in line:
            new_lines.append(line)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.writelines(new_lines)

