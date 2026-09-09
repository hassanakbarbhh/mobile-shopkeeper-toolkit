import re

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    # This is to remove the AppMode.BASIC_USER -> { block inside the click listener
    if "return@setNavigationItemSelectedListener true" in line and (i+1 < len(lines)) and "AppMode.BASIC_USER" in lines[i+1]:
        new_lines.append(line)
        skip = True
        continue
    if skip:
        if "if (menuItem.itemId == R.id.navigation_lock_app) {" in line:
            skip = False
            new_lines.append(line)
        continue
    
    # Fix unresolved reference
    if "R.id.navigation_access_control" in line:
        pass # we'll fix it if needed but let's see if it compiles if we just remove the block
        
    new_lines.append(line)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.writelines(new_lines)
