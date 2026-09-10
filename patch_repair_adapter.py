with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairAdapter.kt", "r") as f:
    content = f.read()

import re

imports = """
import android.graphics.Color
import com.shopkeeper.mobileshop.data.db.entity.RepairStatus
"""
content = content.replace("import com.shopkeeper.mobileshop.databinding.ItemRepairBinding", imports + "import com.shopkeeper.mobileshop.databinding.ItemRepairBinding")

class_old = """class RepairAdapter(
    private val onItemClick: (Repair) -> Unit
) : ListAdapter<Repair, RepairAdapter.ViewHolder>(DiffCallback) {"""
class_new = """class RepairAdapter(
    private val onItemClick: (Repair) -> Unit,
    private val onAdvanceClick: (Repair) -> Unit = {}
) : ListAdapter<Repair, RepairAdapter.ViewHolder>(DiffCallback) {
    var warrantyStatuses: Map<Long, Boolean> = emptyMap()
"""
content = content.replace(class_old, class_new)

bind_old = """        fun bind(r: Repair) {
            binding.tvDevice.text = "${r.deviceBrand} ${r.deviceModel}"
            binding.tvCustomer.text = "${r.customerName} - ${r.customerPhone}"
            binding.tvStatus.text = r.status.name
            binding.tvIssue.text = r.issueDescription
            binding.tvDate.text = "Received: ${r.receivedDate.toDateFormat()}"
            binding.tvCost.text = "Est: ${r.estimatedCost.money()}"
            
            // Set status color
            when(r.status) {
                RepairStatus.RECEIVED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.DIAGNOSING -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.WAITING_PARTS -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.IN_REPAIR -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_partial)
                RepairStatus.COMPLETED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid)
                RepairStatus.DELIVERED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid)
                RepairStatus.CANCELLED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
            }
            
            binding.root.setOnClickListener { onItemClick(r) }
        }"""
bind_new = """        fun bind(r: Repair) {
            binding.tvDevice.text = "${r.deviceBrand} ${r.deviceModel}"
            binding.tvCustomer.text = "${r.customerName} - ${r.customerPhone}"
            binding.tvStatus.text = r.status.name
            binding.tvIssue.text = r.issueDescription
            binding.tvDate.text = "Received: ${r.receivedDate.toDateFormat()}"
            binding.tvCost.text = "Est: ${r.estimatedCost.money()}"
            
            // Set status color
            when(r.status) {
                RepairStatus.RECEIVED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.DIAGNOSING -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.WAITING_PARTS -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                RepairStatus.IN_REPAIR -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_partial)
                RepairStatus.COMPLETED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid)
                RepairStatus.DELIVERED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid)
                RepairStatus.CANCELLED -> binding.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
            }
            
            val hasWarranty = warrantyStatuses[r.id]
            if (hasWarranty == true) {
                binding.tvWarranty.text = "WARRANTY IN"
                binding.tvWarranty.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvWarranty.setTextColor(Color.parseColor("#2E7D32"))
            } else if (hasWarranty == false) {
                binding.tvWarranty.text = "WARRANTY OUT"
                binding.tvWarranty.setBackgroundColor(Color.parseColor("#FFEBEE"))
                binding.tvWarranty.setTextColor(Color.parseColor("#C62828"))
            } else {
                binding.tvWarranty.text = "NO WARRANTY"
            }
            
            binding.btnAdvanceStatus.setOnClickListener { onAdvanceClick(r) }
            binding.root.setOnClickListener { onItemClick(r) }
        }"""
content = content.replace(bind_old, bind_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairAdapter.kt", "w") as f:
    f.write(content)
