with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    for i, line in enumerate(f.readlines()):
        if "googleSignInLauncher" in line:
            print(f"Line {i}: {line.strip()}")
