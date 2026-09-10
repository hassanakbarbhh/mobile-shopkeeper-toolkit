with open("app/src/main/res/layout/item_customer.xml", "r") as f:
    content = f.read()

# Add a tvAging to the linear layout before the ImageView
tv_aging = """
        <TextView android:id="@+id/tvAging" android:layout_width="wrap_content"
            android:layout_height="wrap_content" android:textSize="12sp"
            android:textStyle="bold" android:layout_marginEnd="12dp"
            android:visibility="gone"/>
"""
content = content.replace("<ImageView android:layout_width=\"20dp\" android:layout_height=\"20dp\"", tv_aging + "<ImageView android:layout_width=\"20dp\" android:layout_height=\"20dp\"")

with open("app/src/main/res/layout/item_customer.xml", "w") as f:
    f.write(content)
