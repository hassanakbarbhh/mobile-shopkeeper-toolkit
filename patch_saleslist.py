with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/SalesListFragment.kt", "r") as f:
    content = f.read()

import re

imports = """
import com.shopkeeper.mobileshop.domain.SalesCommissionCalculator
import com.shopkeeper.mobileshop.utils.money
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.InvoiceGenerator", imports + "import com.shopkeeper.mobileshop.utils.InvoiceGenerator")

dialog_old = """            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Invoice #${sale.id} • ${sale.customerName}")
                .setMessage("Date: ${sale.saleDate}\\nSold by: ${sale.sellerName}\\nTotal: Rs.${sale.finalAmount}\\nPayment: ${sale.paymentMethod} (${sale.paymentStatus})\\n\\nItems:\\n$itemsText")"""
dialog_new = """            val commissionCalc = SalesCommissionCalculator()
            val commission = commissionCalc.calculateCommission(sale.finalAmount, 5.0) // 5% standard staff commission
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Invoice #${sale.id} • ${sale.customerName}")
                .setMessage("Date: ${sale.saleDate}\\nSold by: ${sale.sellerName} (Commission: ${commission.money()})\\nTotal: Rs.${sale.finalAmount}\\nPayment: ${sale.paymentMethod} (${sale.paymentStatus})\\n\\nItems:\\n$itemsText")"""
content = content.replace(dialog_old, dialog_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/SalesListFragment.kt", "w") as f:
    f.write(content)
