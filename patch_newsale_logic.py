with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "r") as f:
    content = f.read()

import re

# Insert engines
imports = """
import com.shopkeeper.mobileshop.domain.MarginGuard
import com.shopkeeper.mobileshop.domain.DiscountApprovalGuard
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.money", imports + "import com.shopkeeper.mobileshop.utils.money")

# Patch recalculateTotals
recalculate_totals_old = """    private fun recalculateTotals() {
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)
        
        binding.tvSubtotal.text = subtotal.money()
        binding.tvTotal.text = grandTotal.money()
    }"""
recalculate_totals_new = """    private fun recalculateTotals() {
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)
        
        binding.tvSubtotal.text = subtotal.money()
        binding.tvTotal.text = grandTotal.money()

        // MarginGuard check (assuming an average purchase price or checking all items)
        viewLifecycleOwner.lifecycleScope.launch {
            var allGood = true
            val marginGuard = MarginGuard()
            for (item in cartItems) {
                val product = repository.getProduct(item.productId)
                if (product != null) {
                    val itemSalePrice = (item.totalPrice - (discount / cartItems.size.coerceAtLeast(1))) / item.quantity
                    if (!marginGuard.isMarginAcceptable(itemSalePrice, product.purchasePrice, 3.0)) {
                        allGood = false
                        break
                    }
                }
            }
            binding.tvMarginWarning?.visibility = if (!allGood) View.VISIBLE else View.GONE
        }
    }"""
content = content.replace(recalculate_totals_old, recalculate_totals_new)


# Patch completeSale for DiscountApprovalGuard
complete_sale_old = """        val method = when (binding.chipGroupPayment.checkedChipId) {"""
complete_sale_new = """
        val discountApprovalGuard = DiscountApprovalGuard()
        if (discountApprovalGuard.requiresOwnerApproval(discount, subtotal, 15.0)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Owner Approval Required")
                .setMessage("This discount exceeds the 15% maximum threshold. Please have the owner approve this transaction.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        val method = when (binding.chipGroupPayment.checkedChipId) {"""
content = content.replace(complete_sale_old, complete_sale_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "w") as f:
    f.write(content)
