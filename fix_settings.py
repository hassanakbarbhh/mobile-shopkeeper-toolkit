with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

bad_block = """        binding.btnAbout.setOnClickListener {
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
            findNavController().navigate(R.id.navigation_about)
        }"""

good_block = """        binding.btnAbout.setOnClickListener {
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

content = content.replace(bad_block, good_block)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
