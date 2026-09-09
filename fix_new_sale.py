with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "r") as f:
    content = f.read()

import re

old = """    private fun setupListeners() {
        binding.etProductSearch.setOnClickListener { showProductSearchDialog() }"""

new = """    // Register scanner launcher
    private val barcodeLauncher = registerForActivityResult(com.journeyapps.barcodescanner.ScanContract()) { result ->
        if (result.contents != null) {
            handleScannedBarcode(result.contents)
        }
    }

    private fun handleScannedBarcode(barcode: String) {
        // Find product by barcode and add to cart
        viewLifecycleOwner.lifecycleScope.launch {
            val product = repository.getProductByBarcode(barcode)
            if (product != null) {
                addProductToCart(product)
            } else {
                android.widget.Toast.makeText(requireContext(), "Product not found: $barcode", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addProductToCart(product: com.shopkeeper.mobileshop.data.db.entity.Product) {
        val existing = cartItems.find { it.productId == product.id }
        if (existing != null) {
            val idx = cartItems.indexOf(existing)
            cartItems[idx] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(
                com.shopkeeper.mobileshop.data.db.entity.SaleItem(
                    saleId = 0,
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitPrice = product.price,
                    totalPrice = product.price
                )
            )
        }
        cartAdapter.submitList(cartItems.toList())
        recalculateTotals()
    }

    private fun setupListeners() {
        binding.tilProductSearch.setEndIconOnClickListener {
            barcodeLauncher.launch(com.journeyapps.barcodescanner.ScanOptions().apply {
                setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.ALL_CODE_TYPES)
                setPrompt("Scan a product barcode or QR code")
                setCameraId(0) // Use specific camera of the device
                setBeepEnabled(true)
                setBarcodeImageEnabled(true)
            })
        }
        binding.etProductSearch.setOnClickListener { showProductSearchDialog() }"""

if old in content:
    content = content.replace(old, new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/sales/NewSaleFragment.kt", "w") as f:
    f.write(content)
