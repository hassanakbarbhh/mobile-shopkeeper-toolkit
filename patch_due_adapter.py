with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dues/DueAdapter.kt", "r") as f:
    content = f.read()

imports = """
import android.graphics.Color
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.money", imports + "import com.shopkeeper.mobileshop.utils.money")

bind_old = """        fun bind(sale: Sale) {
            binding.tvCustName.text = sale.customerName
            binding.tvBalance.text = "Due: ${sale.finalAmount.money()}"
            binding.tvDueDate.text = "Invoice #${sale.id} • ${sale.saleDate.dateTimeText()}"
            binding.tvPaidInfo.text = "Status: ${sale.paymentStatus}"
            binding.root.setOnClickListener { onItemClick(sale) }
        }"""
bind_new = """        fun bind(sale: Sale) {
            binding.tvCustName.text = sale.customerName
            binding.tvBalance.text = "Due: ${sale.finalAmount.money()}"
            binding.tvDueDate.text = "Invoice #${sale.id} • ${sale.saleDate.dateTimeText()}"
            
            val analyzer = UdhaarAgingAnalyzer()
            val category = analyzer.getAgingCategory(sale.saleDate) // Assuming due date is sale date + some offset, or just sale date
            binding.tvPaidInfo.text = "Status: ${sale.paymentStatus} • $category"
            
            when {
                category.contains("Critical") -> binding.root.setBackgroundColor(Color.parseColor("#FFCDD2")) // Red
                category.contains("61-90") -> binding.root.setBackgroundColor(Color.parseColor("#FFE0B2")) // Orange
                category.contains("31-60") -> binding.root.setBackgroundColor(Color.parseColor("#FFF9C4")) // Yellow
                else -> binding.root.setBackgroundColor(Color.WHITE)
            }
            
            binding.root.setOnClickListener { onItemClick(sale) }
        }"""
content = content.replace(bind_old, bind_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dues/DueAdapter.kt", "w") as f:
    f.write(content)
