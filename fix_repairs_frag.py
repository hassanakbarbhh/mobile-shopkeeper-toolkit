with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairsFragment.kt", "r") as f:
    content = f.read()
import re
content = content.replace("adapter = RepairAdapter { repair -> showRepairActions(repair) }", """adapter = RepairAdapter({ repair -> showRepairActions(repair) }, { repair ->
            viewLifecycleOwner.lifecycleScope.launch {
                val router = com.shopkeeper.mobileshop.domain.RepairStatusStateRouter()
                val nextStatus = router.getNextStatus(repair.status)
                if (nextStatus != repair.status) {
                    val updated = repair.copy(status = nextStatus)
                    repository.updateRepair(updated)
                    android.widget.Toast.makeText(requireContext(), "Advanced to ${nextStatus.name}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        })""")
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairsFragment.kt", "w") as f:
    f.write(content)
