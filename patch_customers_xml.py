with open("app/src/main/res/layout/fragment_customers.xml", "r") as f:
    content = f.read()

call_today_card = """
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardCallToday"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginHorizontal="16dp"
            android:layout_marginTop="12dp"
            app:cardCornerRadius="12dp"
            app:cardBackgroundColor="#FFF3E0"
            app:strokeColor="#FF9800"
            app:strokeWidth="1dp"
            android:visibility="gone">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="🚨 Call Today (Top 3 Overdue)"
                    android:textSize="14sp"
                    android:textColor="#E65100"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp"/>
                <TextView
                    android:id="@+id/tvCallTodayList"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textSize="14sp"
                    android:textColor="#BF360C"/>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>
"""

content = content.replace("android:layout_marginTop=\"12dp\"", "android:layout_marginTop=\"4dp\"", 1)
content = content.replace("<LinearLayout\n            android:layout_width=\"match_parent\"\n            android:layout_height=\"wrap_content\"\n            android:orientation=\"horizontal\"", call_today_card + "\n        <LinearLayout\n            android:layout_width=\"match_parent\"\n            android:layout_height=\"wrap_content\"\n            android:orientation=\"horizontal\"")

with open("app/src/main/res/layout/fragment_customers.xml", "w") as f:
    f.write(content)
