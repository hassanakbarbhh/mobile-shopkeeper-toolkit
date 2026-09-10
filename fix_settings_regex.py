import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

bad_pattern = re.compile(
    r"binding\.btnAbout\.setOnClickListener \{.*?\} \s* binding\.btnViewCrashLogs\.setOnClickListener \{.*?\} \s* findNavController\(\)\.navigate\(R\.id\.navigation_about\)\s*\}",
    re.DOTALL
)

def replacer(match):
    return """binding.btnAbout.setOnClickListener {
            findNavController().navigate(R.id.navigation_about)
        }
        
        binding.btnViewCrashLogs.setOnClickListener {
            val logs = GlobalExceptionHandler.readLogs(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Crash Logs")
                .setMessage(if (logs.isBlank()) "No crashes recorded." else logs)
                .setPositiveButton("Close", null)
                .show()
        }"""

new_content = bad_pattern.sub(replacer, content)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(new_content)
