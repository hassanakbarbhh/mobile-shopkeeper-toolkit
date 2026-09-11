import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dashboard/DashboardFragment.kt", "r") as f:
    content = f.read()

# Replace binding. with _binding?. in all viewLifecycleOwner.lifecycleScope.launch blocks
content = re.sub(r'binding\.(tvTodaySales|tvTodayProfit|tvSalesCount|tvInventoryValue|tvProductCount|tvPendingPayments|tvActiveRepairs|cardLowStock|tvLowStockCount|cardDeadStock|tvDeadStockCount|tvDeadStockCapital)\.', r'_binding?.\1?.', content)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/dashboard/DashboardFragment.kt", "w") as f:
    f.write(content)
