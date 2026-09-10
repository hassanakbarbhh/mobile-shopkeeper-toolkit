with open("app/src/main/res/layout/fragment_new_sale.xml", "r") as f:
    content = f.read()

banner = """
    <TextView
        android:id="@+id/tvMarginWarning"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFCDD2"
        android:textColor="#B71C1C"
        android:padding="8dp"
        android:text="Warning: Profit margin is critically low (< 3%)"
        android:textStyle="bold"
        android:gravity="center"
        android:visibility="gone"/>
"""
content = content.replace("<com.google.android.material.card.MaterialCardView", banner + "\n    <com.google.android.material.card.MaterialCardView", 1)

with open("app/src/main/res/layout/fragment_new_sale.xml", "w") as f:
    f.write(content)
