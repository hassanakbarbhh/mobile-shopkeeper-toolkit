with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

content = content.replace("        binding.btnManageGoogleAuth.setOnClickListener {\n            showGoogleAuthManagementDialog()\n        }", "")
content = content.replace("        binding.btnSetupMfa.setOnClickListener {\n            showMfaSetupDialog()\n        }", "")
content = content.replace("        binding.btnCheckFirebaseAuth.setOnClickListener {\n            runFirebaseDiagnostics()\n        }", "")

# And tvSettingsGoogleSubtitle
content = content.replace("binding.tvSettingsGoogleSubtitle.text =", "// ")

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
