with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

imports = """
import com.shopkeeper.mobileshop.utils.TotpAuthenticator
import android.widget.ImageView
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.ThemeManager", imports + "import com.shopkeeper.mobileshop.utils.ThemeManager")

listener = """        binding.btnManageGoogleAuth.setOnClickListener {
            showGoogleAuthManagementDialog()
        }
        
        binding.btnSetupMfa.setOnClickListener {
            val authenticator = TotpAuthenticator()
            val secret = "JBSWY3DPEHPK3PXP" // Mock secret for generation
            val bitmap = authenticator.generateQrCodeForAuthenticator(secret, "Owner", "MobileShopkeeper")
            
            val dialogView = layoutInflater.inflate(R.layout.dialog_mfa_setup, null)
            val ivQr = dialogView.findViewById<ImageView>(R.id.ivQrCode)
            val etCode = dialogView.findViewById<android.widget.EditText>(R.id.etMfaCode)
            ivQr.setImageBitmap(bitmap)
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("2FA Setup (Google Authenticator)")
                .setView(dialogView)
                .setPositiveButton("Verify & Enable") { _, _ ->
                    val code = etCode.text.toString()
                    if (authenticator.verifyTotpCode(secret, code)) {
                        Toast.makeText(requireContext(), "2FA Enabled! Device trusted for 30 days.", Toast.LENGTH_LONG).show()
                        // Save trust date to preferences or DB
                        val prefs = requireContext().getSharedPreferences("mfa_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putLong("last_trust_date", System.currentTimeMillis()).apply()
                    } else {
                        Toast.makeText(requireContext(), "Invalid Code. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
"""
content = content.replace("""        binding.btnManageGoogleAuth.setOnClickListener {
            showGoogleAuthManagementDialog()
        }""", listener)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
