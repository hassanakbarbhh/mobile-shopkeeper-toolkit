with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "r") as f:
    content = f.read()

import re

old_total = """    private fun recalculateTotals() {
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)

        binding.tvSubtotal.text = subtotal.money()
        binding.tvTotal.text = grandTotal.money()
    }"""
new_total = """    private fun recalculateTotals() {
        viewLifecycleOwner.lifecycleScope.launch {
            val subtotal = cartItems.sumOf { it.totalPrice }
            val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
            val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
            val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)

            binding.tvSubtotal.text = subtotal.money()
            binding.tvTotal.text = grandTotal.money()

            var totalPurchasePrice = 0.0
            for (item in cartItems) {
                val product = repository.getProduct(item.productId)
                totalPurchasePrice += (product?.purchasePrice ?: 0.0) * item.quantity
            }
            val marginGuard = MarginGuard()
            val isAcceptable = marginGuard.isMarginAcceptable(grandTotal, totalPurchasePrice, 3.0)
            binding.tvMarginWarning.visibility = if (!isAcceptable && totalPurchasePrice > 0) android.view.View.VISIBLE else android.view.View.GONE
        }
    }"""
content = content.replace(old_total, new_total)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "w") as f:
    f.write(content)
