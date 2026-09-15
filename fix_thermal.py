import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "r") as f:
    text = f.read()

# Make sure imports exist for AppDatabase and lifecycleScope
if "import com.shopkeeper.mobileshop.data.db.AppDatabase" not in text:
    text = text.replace("import android.os.Bundle", "import android.os.Bundle\nimport com.shopkeeper.mobileshop.data.db.AppDatabase")
if "import androidx.lifecycle.lifecycleScope" not in text:
    text = text.replace("import android.os.Bundle", "import android.os.Bundle\nimport androidx.lifecycle.lifecycleScope\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.withContext")

# Replace btnTestThermalPrint
sale_pattern = r'binding\.btnTestThermalPrint\.setOnClickListener \{.*?showSaleReceiptDialog.*?\}'
sale_replacement = """binding.btnTestThermalPrint.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val lastSale = db.saleDao().getAllSales().firstOrNull()
                withContext(Dispatchers.Main) {
                    if (lastSale != null) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            val items = db.saleDao().getSaleItems(lastSale.id)
                            withContext(Dispatchers.Main) {
                                ThermalPrintHelper.showSaleReceiptDialog(requireContext(), lastSale, items)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No real sales found in database.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }"""
text = re.sub(sale_pattern, sale_replacement, text, flags=re.DOTALL)

# Replace btnTestRepairTag
repair_pattern = r'binding\.btnTestRepairTag\.setOnClickListener \{.*?showRepairTagDialog.*?\}'
repair_replacement = """binding.btnTestRepairTag.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val lastRepair = db.repairDao().getAllRepairs().firstOrNull()
                withContext(Dispatchers.Main) {
                    if (lastRepair != null) {
                        ThermalPrintHelper.showRepairTagDialog(requireContext(), lastRepair)
                    } else {
                        Toast.makeText(requireContext(), "No real repairs found in database.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }"""
text = re.sub(repair_pattern, repair_replacement, text, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/settings/SettingsFragment.kt", "w") as f:
    f.write(text)
