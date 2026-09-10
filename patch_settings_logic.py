import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

import_str = """import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import android.util.Log"""

content = content.replace("import com.shopkeeper.mobileshop.R", import_str + "\nimport com.shopkeeper.mobileshop.R")

bind_btn = """        binding.btnViewCrashLogs.setOnClickListener {
            val logs = GlobalExceptionHandler.readLogs(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Crash Logs")
                .setMessage(logs.ifBlank { "No crash logs found." })
                .setPositiveButton("OK", null)
                .setNeutralButton("Clear") { _, _ ->
                    GlobalExceptionHandler.clearLogs(requireContext())
                }
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }"""

content = re.sub(r'        binding\.btnViewCrashLogs\.setOnClickListener \{.*?\.show\(\)\n        \}', bind_btn, content, flags=re.DOTALL)

delete_fun = """    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account & Data")
            .setMessage("Are you sure you want to delete your account and all cloud data? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performAccountDeletion()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performAccountDeletion() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You are not logged in to cloud.", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()
        
        Toast.makeText(requireContext(), "Deleting cloud data...", Toast.LENGTH_LONG).show()
        
        // Delete users document
        db.collection("users").document(uid).delete()
        
        // Delete shops
        db.collection("shops").whereEqualTo("ownerUid", uid).get().addOnSuccessListener { snaps ->
            val batch = db.batch()
            for (doc in snaps) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnCompleteListener {
                // Now delete user
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Account & Data Deleted.", Toast.LENGTH_LONG).show()
                        val intent = Intent(requireContext(), com.shopkeeper.mobileshop.ui.auth.LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val e = task.exception
                        if (e is FirebaseAuthRecentLoginRequiredException) {
                            Toast.makeText(requireContext(), "Please re-login first to delete account.", Toast.LENGTH_LONG).show()
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(requireContext(), com.shopkeeper.mobileshop.ui.auth.LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteAccount", "Error", e)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to access cloud data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showThemePickerDialog() {"""

content = content.replace("    private fun showThemePickerDialog() {", delete_fun)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
