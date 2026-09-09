with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'menu\.findItem\(R\.id\.navigation_access_control\)\?\.isVisible = (true|false)\n', '', content)
content = re.sub(r'if \(menuItem\.itemId == R\.id\.navigation_access_control\) \{\n[^\}]+return@setNavigationItemSelectedListener true\n\s*\}\n\s*', '', content)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.write(content)
