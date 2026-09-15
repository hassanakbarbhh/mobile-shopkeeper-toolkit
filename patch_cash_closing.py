import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    text = f.read()

closing_pattern = r'binding\.btnCashClosing\.setOnClickListener \{.*?\.show\(\)\n        \}'
closing_replacement = """binding.btnCashClosing.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000))
                val todaySales = db.saleDao().getAllSalesList().filter { it.saleDate >= todayStart }.sumOf { it.finalAmount }
                
                withContext(Dispatchers.Main) {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_cash_closing, null)
                    val etOpening = dialogView.findViewById<android.widget.EditText>(R.id.etOpening)
                    val etCashIn = dialogView.findViewById<android.widget.EditText>(R.id.etCashIn)
                    val etCashOut = dialogView.findViewById<android.widget.EditText>(R.id.etCashOut)
                    val etCounted = dialogView.findViewById<android.widget.EditText>(R.id.etCounted)
                    val tvVariance = dialogView.findViewById<android.widget.TextView>(R.id.tvVariance)
                    
                    etCashIn.setText(todaySales.toString())
                    
                    etCounted.doAfterTextChanged {
                        val opening = etOpening.text.toString().toDoubleOrNull() ?: 0.0
                        val inCash = etCashIn.text.toString().toDoubleOrNull() ?: 0.0
                        val outCash = etCashOut.text.toString().toDoubleOrNull() ?: 0.0
                        val counted = etCounted.text.toString().toDoubleOrNull() ?: 0.0
                        
                        val variance = counted - (opening + inCash - outCash)
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
                            val opening = etOpening.text.toString().toDoubleOrNull() ?: 0.0
                            val inCash = etCashIn.text.toString().toDoubleOrNull() ?: 0.0
                            val outCash = etCashOut.text.toString().toDoubleOrNull() ?: 0.0
                            val counted = etCounted.text.toString().toDoubleOrNull() ?: 0.0
                            val variance = counted - (opening + inCash - outCash)
                            
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                db.cashClosingDao().insert(
                                    com.shopkeeper.mobileshop.data.db.entity.CashClosing(
                                        closingDate = System.currentTimeMillis(),
                                        openingCash = opening,
                                        cashIn = inCash,
                                        cashOut = outCash,
                                        countedCash = counted,
                                        variance = variance,
                                        signedBy = "Current User"
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Signed off cash closing.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }"""
text = re.sub(closing_pattern, closing_replacement, text, flags=re.DOTALL)

# Add .getAllSalesList() to SaleDao if not exists
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(text)
