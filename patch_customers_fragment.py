with open("app/src/main/java/com/shopkeeper/mobileshop/ui/customers/CustomersFragment.kt", "r") as f:
    content = f.read()

import re

# Add UdhaarAgingAnalyzer import
imports = """
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
"""
content = content.replace("import kotlinx.coroutines.launch", imports + "import kotlinx.coroutines.launch")

# Patch onViewCreated to fetch pending sales, build map, and update UI
load_logic = """
        viewLifecycleOwner.lifecycleScope.launch {
            val pendingSales = repository.getPendingSales()
            val map = mutableMapOf<Long, Long>()
            for (sale in pendingSales) {
                if (sale.customerId != null) {
                    val currentOldest = map[sale.customerId]
                    if (currentOldest == null || sale.date < currentOldest) {
                        map[sale.customerId] = sale.date
                    }
                }
            }
            adapter.pendingMap = map
            
            // Build Call Today card
            if (map.isNotEmpty()) {
                val analyzer = UdhaarAgingAnalyzer()
                val sorted = map.toList().sortedBy { it.second }.take(3)
                val textBuilder = StringBuilder()
                for ((custId, date) in sorted) {
                    val customer = currentCustomers.find { it.id == custId }
                    if (customer != null) {
                        textBuilder.append("• ${customer.name} - ${analyzer.getAgingCategory(date)}\n")
                    }
                }
                if (textBuilder.isNotEmpty()) {
                    binding.cardCallToday.visibility = View.VISIBLE
                    binding.tvCallTodayList.text = textBuilder.toString().trim()
                } else {
                    binding.cardCallToday.visibility = View.GONE
                }
            } else {
                binding.cardCallToday.visibility = View.GONE
            }
            
            // Re-submit to refresh aging
            if (currentCustomers.isNotEmpty()) {
                adapter.submitList(currentCustomers.toList())
            }
        }
"""
content = content.replace("        setupSearch()", load_logic + "\n        setupSearch()")

# Also, update currentCustomers when the list updates so we can resolve names.
collect_old = """                currentCustomers = customers
                adapter.submitList(customers)"""
collect_new = """                currentCustomers = customers
                adapter.submitList(customers)
                // Trigger re-calc of top-3 if needed
                viewLifecycleOwner.lifecycleScope.launch {
                    val pendingSales = repository.getPendingSales()
                    val map = mutableMapOf<Long, Long>()
                    for (sale in pendingSales) {
                        if (sale.customerId != null) {
                            val currentOldest = map[sale.customerId]
                            if (currentOldest == null || sale.date < currentOldest) {
                                map[sale.customerId] = sale.date
                            }
                        }
                    }
                    adapter.pendingMap = map
                    if (map.isNotEmpty()) {
                        val analyzer = UdhaarAgingAnalyzer()
                        val sorted = map.toList().sortedBy { it.second }.take(3)
                        val textBuilder = StringBuilder()
                        for ((custId, date) in sorted) {
                            val customer = currentCustomers.find { it.id == custId }
                            if (customer != null) {
                                textBuilder.append("• ${customer.name} - ${analyzer.getAgingCategory(date)}\n")
                            }
                        }
                        if (textBuilder.isNotEmpty()) {
                            binding.cardCallToday.visibility = View.VISIBLE
                            binding.tvCallTodayList.text = textBuilder.toString().trim()
                        }
                    }
                    adapter.notifyDataSetChanged()
                }"""
content = content.replace(collect_old, collect_new)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/customers/CustomersFragment.kt", "w") as f:
    f.write(content)
