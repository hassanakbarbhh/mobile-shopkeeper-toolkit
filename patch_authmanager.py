with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "r") as f:
    code = f.read()

import re

# Fix syntax error at end of file (471:34 Syntax error: Expecting a top level declaration)
code = code.rstrip()
while code.endswith("}"):
    code = code[:-1].rstrip()
# Add back exactly one to close the object (updateLocalRole closes itself)
code = code + "\n    }\n}\n"

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/UserAuthManager.kt", "w") as f:
    f.write(code)
