with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

imports = """
import com.shopkeeper.mobileshop.utils.GlobalExceptionHandler
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.TotpAuthenticator", imports + "import com.shopkeeper.mobileshop.utils.TotpAuthenticator")

listener = """        binding.btnAbout.setOnClickListener {
            // ... (rest of about logic if any, wait, btnAbout is just showing about text)
        }
        
        binding.btnViewCrashLogs.setOnClickListener {
            val logs = GlobalExceptionHandler.readLogs(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Crash Logs")
                .setMessage(if (logs.isBlank()) "No crashes recorded." else logs)
                .setPositiveButton("Close", null)
                .show()
        }
"""
content = content.replace("""        binding.btnAbout.setOnClickListener {""", listener)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
