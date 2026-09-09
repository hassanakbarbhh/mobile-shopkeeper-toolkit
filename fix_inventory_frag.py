with open("app/src/main/java/com/shopkeeper/mobileshop/ui/inventory/InventoryFragment.kt", "r") as f:
    content = f.read()

import re

old = """        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }"""

new = """        // Register scanner launcher
        val barcodeLauncher = registerForActivityResult(com.journeyapps.barcodescanner.ScanContract()) { result ->
            if (result.contents != null) {
                binding.etSearch.setText(result.contents)
                viewModel.setSearchQuery(result.contents)
            }
        }
        
        binding.tilSearch.setEndIconOnClickListener {
            barcodeLauncher.launch(com.journeyapps.barcodescanner.ScanOptions().apply {
                setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.ALL_CODE_TYPES)
                setPrompt("Scan a product barcode or QR code")
                setCameraId(0) // Use specific camera of the device
                setBeepEnabled(true)
                setBarcodeImageEnabled(true)
            })
        }
        
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }"""

if old in content:
    content = content.replace(old, new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/inventory/InventoryFragment.kt", "w") as f:
    f.write(content)
