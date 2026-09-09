with open("app/src/main/res/layout/fragment_new_sale.xml", "r") as f:
    content = f.read()

# Clean up
content = content.replace('        android:id="@+id/tilProductSearch"\n', '')

# Insert specifically for product search
old = """    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        app:startIconDrawable="@drawable/ic_search">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etProductSearch"
"""

new = """    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilProductSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        app:startIconDrawable="@drawable/ic_search"
        app:endIconMode="custom"
        app:endIconDrawable="@drawable/ic_barcode">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etProductSearch"
"""
if old in content:
    content = content.replace(old, new)

with open("app/src/main/res/layout/fragment_new_sale.xml", "w") as f:
    f.write(content)
