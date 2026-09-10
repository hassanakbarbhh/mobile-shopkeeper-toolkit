with open("app/src/main/res/layout/fragment_dashboard.xml", "r") as f:
    content = f.read()

import re

# Add today's net profit to tvTodaySales card
today_sales_card = """                    <TextView
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
                        android:textSize="28sp"
                        android:textStyle="bold"
                        android:textColor="@color/primary"/>
                    <TextView
                        android:id="@+id/tvSalesCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 transactions today"
                        android:textSize="12sp"
                        android:textColor="?android:textColorSecondary"/>"""

new_sales_card = """                    <TextView
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
                        android:layout_marginTop="8dp"/>"""

content = content.replace(today_sales_card, new_sales_card)

with open("app/src/main/res/layout/fragment_dashboard.xml", "w") as f:
    f.write(content)
