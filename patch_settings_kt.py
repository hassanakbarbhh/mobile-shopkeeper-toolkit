with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    content = f.read()

import re

imports = """
import com.shopkeeper.mobileshop.domain.CashClosingReconciler
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.money", imports + "import com.shopkeeper.mobileshop.utils.money")

click_listener = """
        binding.btnCashClosing.setOnClickListener {
            // Basic mock dialog for Cash Closing
            val dialogView = layoutInflater.inflate(R.layout.dialog_cash_closing, null)
            val etOpening = dialogView.findViewById<android.widget.EditText>(R.id.etOpening)
            val etCashIn = dialogView.findViewById<android.widget.EditText>(R.id.etCashIn)
            val etCashOut = dialogView.findViewById<android.widget.EditText>(R.id.etCashOut)
            val etCounted = dialogView.findViewById<android.widget.EditText>(R.id.etCounted)
            val tvVariance = dialogView.findViewById<android.widget.TextView>(R.id.tvVariance)
            
            etCounted.doAfterTextChanged {
                val opening = etOpening.text.toString().toDoubleOrNull() ?: 0.0
                val inCash = etCashIn.text.toString().toDoubleOrNull() ?: 0.0
                val outCash = etCashOut.text.toString().toDoubleOrNull() ?: 0.0
                val counted = etCounted.text.toString().toDoubleOrNull() ?: 0.0
                
                val reconciler = CashClosingReconciler()
                val variance = reconciler.reconcile(opening, inCash, outCash, counted)
                tvVariance.text = "Variance: " + variance.money()
                if (variance < 0) {
                    tvVariance.setTextColor(android.graphics.Color.RED)
                } else {
                    tvVariance.setTextColor(android.graphics.Color.GREEN)
                }
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Daily Cash Closing")
                .setView(dialogView)
                .setPositiveButton("Sign-off") { _, _ ->
                    Toast.makeText(requireContext(), "Signed off cash closing.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
"""
content = content.replace("binding.btnAbout.setOnClickListener {", click_listener + "\n        binding.btnAbout.setOnClickListener {")

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(content)
