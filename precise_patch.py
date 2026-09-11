with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    text = f.read()

# 1. btnManageGoogleAuth
text = text.replace(
    "        binding.btnManageGoogleAuth.setOnClickListener {\n            showGoogleAuthManagementDialog()\n        }",
    "        // btnManageGoogleAuth removed"
)

# 2. tvSettingsGoogleSubtitle
text = text.replace('binding.tvSettingsGoogleSubtitle.text = "👑 Verified Shop Owner • Full Master Access Active"', '//')
text = text.replace('binding.tvSettingsGoogleSubtitle.text = "💼 Verified Staff Account (Owner: $ownerEmail)"', '//')
text = text.replace('binding.tvSettingsGoogleSubtitle.text = "🛡️ Verified Google Identity • Anti-Leak Owner Protection"', '//')

# 3. btnSetupMfa
# It spans multiple lines, let's remove from "binding.btnSetupMfa.setOnClickListener {" down to the next "        }"
import re
text = re.sub(r'        binding\.btnSetupMfa\.setOnClickListener \{.*?\n        \}', '        // btnSetupMfa removed', text, flags=re.DOTALL)

# 4. btnCheckFirebaseAuth
text = re.sub(r'        binding\.btnCheckFirebaseAuth\.setOnClickListener \{.*?\n        \}', '        // btnCheckFirebaseAuth removed', text, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(text)
