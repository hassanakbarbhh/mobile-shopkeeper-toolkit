with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    text = f.read()

imports = """import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
"""

if "import kotlinx.coroutines.withContext" not in text:
    text = text.replace("import kotlinx.coroutines.launch", imports + "import kotlinx.coroutines.launch")

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(text)
