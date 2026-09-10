with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairsFragment.kt", "r") as f:
    content = f.read()

import re

imports = """
import com.shopkeeper.mobileshop.domain.WarrantyExpirationTracker
import com.shopkeeper.mobileshop.domain.RepairStatusStateRouter
"""
content = content.replace("import kotlinx.coroutines.launch", imports + "import kotlinx.coroutines.launch")

# We need to supply the new onAdvanceClick argument.
adapter_old = """        adapter = RepairAdapter { repair ->
            showRepairDetailsDialog(repair)
        }"""
adapter_new = """        adapter = RepairAdapter({ repair ->
            showRepairDetailsDialog(repair)
        }, { repair ->
            viewLifecycleOwner.lifecycleScope.launch {
                val router = RepairStatusStateRouter()
                val nextStatus = router.getNextStatus(repair.status)
                if (nextStatus != repair.status) {
                    val updated = repair.copy(status = nextStatus)
                    repository.updateRepair(updated)
                    Toast.makeText(requireContext(), "Advanced to ${nextStatus.name}", Toast.LENGTH_SHORT).show()
                }
            }
        })"""
content = content.replace(adapter_old, adapter_new)

# Update warranty list on load
load_old = """        viewLifecycleOwner.lifecycleScope.launch {
            repository.allRepairs.collectLatest { repairs ->
                adapter.submitList(repairs)
            }
        }"""
load_new = """        viewLifecycleOwner.lifecycleScope.launch {
            repository.allRepairs.collectLatest { repairs ->
                val warrantyTracker = WarrantyExpirationTracker()
                val map = mutableMapOf<Long, Boolean>()
                for (repair in repairs) {
                    if (repair.imei.isNotEmpty()) {
                        // Assuming 3 months warranty for all devices here for logic hook
                        val product = repository.getProductByBarcode(repair.imei)
                        if (product != null) {
                            val saleItems = AppDatabase.getDatabase(requireContext()).saleDao().getSaleItemsByProductId(product.id)
                            if (saleItems.isNotEmpty()) {
                                // Find sale date
                                val sale = AppDatabase.getDatabase(requireContext()).saleDao().getSaleById(saleItems.first().saleId)
                                if (sale != null) {
                                    map[repair.id] = warrantyTracker.isWarrantyValid(sale.date, 3)
                                }
                            }
                        }
                    }
                }
                adapter.warrantyStatuses = map
                adapter.submitList(repairs)
                adapter.notifyDataSetChanged()
            }
        }"""
content = content.replace(load_old, load_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairsFragment.kt", "w") as f:
    f.write(content)
