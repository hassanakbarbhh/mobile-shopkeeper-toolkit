with open("app/src/main/res/layout/fragment_inventory.xml", "r") as f:
    content = f.read()

old = """            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                app:startIconDrawable="@drawable/ic_search">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etSearch\""""

new = """            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilSearch"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                app:startIconDrawable="@drawable/ic_search"
                app:endIconMode="custom"
                app:endIconDrawable="@drawable/ic_barcode">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etSearch\""""

if old in content:
    content = content.replace(old, new)

with open("app/src/main/res/layout/fragment_inventory.xml", "w") as f:
    f.write(content)
