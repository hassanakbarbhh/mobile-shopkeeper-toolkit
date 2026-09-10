with open("app/src/main/res/layout/fragment_dashboard.xml", "r") as f:
    content = f.read()

import re

# Find the exact block we need to replace and replace it
# We know tvTodaySales exists.
old_block = re.search(r'<TextView\s+android:layout_width="wrap_content"\s+android:layout_height="wrap_content"\s+android:text="Today\'s Sales".*?tvSalesCount.*?</LinearLayout>', content, re.DOTALL)
if old_block:
    new_block = """<TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Today's Sales"
                        android:textSize="14sp"
                        android:textColor="?android:textColorSecondary"/>
                    <TextView
                        android:id="@+id/tvTodaySales"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₹0"
                        android:textSize="24sp"
                        android:textStyle="bold"
                        android:textColor="@color/primary"/>
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Net Profit"
                        android:textSize="14sp"
                        android:textColor="?android:textColorSecondary"
                        android:layout_marginTop="8dp"/>
                    <TextView
                        android:id="@+id/tvTodayProfit"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="₹0"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="#4CAF50"/>
                    <TextView
                        android:id="@+id/tvSalesCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 transactions today"
                        android:textSize="12sp"
                        android:textColor="?android:textColorSecondary"
                        android:layout_marginTop="8dp"/>
                </LinearLayout>"""
    content = content.replace(old_block.group(0), new_block)

with open("app/src/main/res/layout/fragment_dashboard.xml", "w") as f:
    f.write(content)
