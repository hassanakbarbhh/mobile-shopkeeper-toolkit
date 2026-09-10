with open("app/src/main/res/layout/fragment_dashboard.xml", "r") as f:
    content = f.read()
import re
new_content = re.sub(
    r'<TextView[^>]*android:id="@+id/tvTodaySales"[^>]*>.*?<TextView[^>]*android:id="@+id/tvSalesCount"[^>]*>',
    """<TextView
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
                        android:layout_marginTop="8dp"/>""",
    content, flags=re.DOTALL
)
with open("app/src/main/res/layout/fragment_dashboard.xml", "w") as f:
    f.write(new_content)
