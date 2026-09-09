with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "if (menuItem.itemId == R.id.navigation_access_control)" in line:
        skip = True
        continue
    if skip and "if (menuItem.itemId == R.id.navigation_lock_app)" in line:
        skip = False
        new_lines.append(line)
        continue
    if not skip:
        new_lines.append(line)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.writelines(new_lines)
