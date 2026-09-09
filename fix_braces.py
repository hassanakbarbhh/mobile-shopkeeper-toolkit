with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

# Let's count { and }
lines = code.split("\n")
indent = 0
for i, line in enumerate(lines):
    if "{" in line:
        indent += line.count("{")
    if "}" in line:
        indent -= line.count("}")
    if indent < 0:
        print(f"Object closed prematurely at line {i+1}: {line}")
        indent = 0
