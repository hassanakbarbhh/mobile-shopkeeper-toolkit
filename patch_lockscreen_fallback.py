import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "r") as f:
    content = f.read()

# Replace the fallback in googleSignInLauncher
old_catch = """            } catch (e: ApiException) {
                // If Play Services is not available or local developer preview, offer email entry fallback
                showGoogleAccountEntryDialog()
            }"""
new_catch = """            } catch (e: ApiException) {
                // Real Google Auth failed (likely missing SHA-1 or google-services.json mismatch)
                Toast.makeText(this, "Google Sign-In failed (Code ${e.statusCode}). Please ensure SHA-1 is added to Firebase.", Toast.LENGTH_LONG).show()
            }"""
content = content.replace(old_catch, new_catch)

# Replace the fallback in launchGoogleSignIn
old_launch = """    private fun launchGoogleSignIn() {
        try {
            val client = GoogleAuthManager.getGoogleSignInClient(this)
            googleSignInLauncher.launch(client.signInIntent)
        } catch (e: Exception) {
            showGoogleAccountEntryDialog()
        }
    }"""
new_launch = """    private fun launchGoogleSignIn() {
        try {
            val client = GoogleAuthManager.getGoogleSignInClient(this)
            googleSignInLauncher.launch(client.signInIntent)
        } catch (e: Exception) {
             Toast.makeText(this, "Failed to launch Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }"""
content = content.replace(old_launch, new_launch)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/auth/LockScreenActivity.kt", "w") as f:
    f.write(content)
