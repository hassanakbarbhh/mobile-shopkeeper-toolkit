import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

content = re.sub(r'        binding\.btnCheckFirebaseAuth\.setOnClickListener \{.*?\}\n', '', content, flags=re.DOTALL)
content = re.sub(r'        binding\.btnSetupMfa\.setOnClickListener \{.*?\}\n', '', content, flags=re.DOTALL)
content = re.sub(r'        binding\.btnManageGoogleAuth\.setOnClickListener \{.*?\}\n', '', content, flags=re.DOTALL)
content = re.sub(r'        // Google Auth Status.*?binding\.tvSettingsGoogleSubtitle\.text = "🛡️ Verified Google Identity"\n        \}', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
