with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dues/DuesFragment.kt", "r") as f:
    content = f.read()

import re
imports = """
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
import android.net.Uri
import android.content.Intent
"""
content = content.replace("import com.shopkeeper.mobileshop.utils.money", imports + "import com.shopkeeper.mobileshop.utils.money")

old_collect = """        viewLifecycleOwner.lifecycleScope.launch {
            repository.dueSales.collectLatest { list ->
                adapter.submitList(list)
                binding.tvNoDues.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }"""
new_collect = """        viewLifecycleOwner.lifecycleScope.launch {
            repository.dueSales.collectLatest { list ->
                adapter.submitList(list)
                binding.tvNoDues.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                
                val analyzer = UdhaarAgingAnalyzer()
                val criticals = list.filter { analyzer.getAgingCategory(it.saleDate).contains("Critical") }
                if (criticals.isNotEmpty()) {
                    binding.cardCallToday.visibility = View.VISIBLE
                    val top3 = criticals.take(3)
                    val text = top3.joinToString("\\n") { "• ${it.customerName}: ${it.finalAmount.money()}" }
                    binding.tvCallTodayList.text = text
                    binding.cardCallToday.setOnClickListener {
                        // Action could be to call the first one or open dialer
                        // We'll leave it as an alert for now.
                    }
                } else {
                    binding.cardCallToday.visibility = View.GONE
                }
            }
        }"""
content = content.replace(old_collect, new_collect)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dues/DuesFragment.kt", "w") as f:
    f.write(content)
