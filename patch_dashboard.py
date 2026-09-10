with open("app/src/main/res/layout/fragment_dashboard.xml", "r") as f:
    content = f.read()

import re

# We can find "<TextView android:id=\"@+id/tvTotalSales\"" and see its structure.
# Let's just insert a new card dead stock after cardLowStock
dead_stock_card = """
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardDeadStock"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="2dp"
            android:visibility="gone">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="16dp"
                android:gravity="center_vertical">
                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@drawable/ic_warning"
                    android:layout_marginEnd="12dp"
                    app:tint="#F44336"/>
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/tvDeadStockCount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 dead stock items"
                        android:textSize="14sp"
                        android:textColor="#F44336"
                        android:textStyle="bold"/>
                    <TextView
                        android:id="@+id/tvDeadStockCapital"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Locked Capital: 0"
                        android:textSize="12sp"/>
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>
"""

content = content.replace("</com.google.android.material.card.MaterialCardView>\n\n        <TextView\n            android:layout_width=\"wrap_content\"\n            android:layout_height=\"wrap_content\"\n            android:text=\"Quick Actions\"", "</com.google.android.material.card.MaterialCardView>\n" + dead_stock_card + "\n        <TextView\n            android:layout_width=\"wrap_content\"\n            android:layout_height=\"wrap_content\"\n            android:text=\"Quick Actions\"")

with open("app/src/main/res/layout/fragment_dashboard.xml", "w") as f:
    f.write(content)
