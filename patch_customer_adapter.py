with open("app/src/main/java/com/shopkeeper/mobileshop/ui/customers/CustomerAdapter.kt", "r") as f:
    content = f.read()

import re

imports = """
import android.graphics.Color
import android.view.View
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
"""
content = content.replace("import com.shopkeeper.mobileshop.databinding.ItemCustomerBinding", imports + "import com.shopkeeper.mobileshop.databinding.ItemCustomerBinding")

# We will change the class to accept a map of customerId to oldest pending sale date
class_old = """class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(DiffCallback) {"""
class_new = """class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(DiffCallback) {
    var pendingMap: Map<Long, Long> = emptyMap()
    val agingAnalyzer = UdhaarAgingAnalyzer()
"""
content = content.replace(class_old, class_new)

# Update bind method
bind_old = """        fun bind(c: Customer) {
            binding.tvName.text = c.name
            binding.tvPhone.text = c.phone
            binding.tvInitials.text = c.name.take(1).uppercase()
            binding.root.setOnClickListener { onItemClick(c) }
        }"""
bind_new = """        fun bind(c: Customer) {
            binding.tvName.text = c.name
            binding.tvPhone.text = c.phone
            binding.tvInitials.text = c.name.take(1).uppercase()
            binding.root.setOnClickListener { onItemClick(c) }
            
            val oldestSaleDate = pendingMap[c.id]
            if (oldestSaleDate != null) {
                binding.tvAging.visibility = View.VISIBLE
                val aging = agingAnalyzer.getAgingCategory(oldestSaleDate)
                binding.tvAging.text = aging
                when (aging) {
                    "0-30 Days Overdue" -> binding.tvAging.setTextColor(Color.parseColor("#F57F17")) // Yellow
                    "31-60 Days Overdue" -> binding.tvAging.setTextColor(Color.parseColor("#E65100")) // Orange
                    "61-90 Days Overdue", "90+ Days Overdue (Critical)" -> binding.tvAging.setTextColor(Color.parseColor("#B71C1C")) // Red
                    else -> binding.tvAging.setTextColor(Color.GRAY)
                }
            } else {
                binding.tvAging.visibility = View.GONE
            }
        }"""
content = content.replace(bind_old, bind_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/customers/CustomerAdapter.kt", "w") as f:
    f.write(content)
