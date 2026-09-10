with open("app/src/main/res/layout/item_repair.xml", "r") as f:
    content = f.read()

# Add Warranty badge and Advance Status button
warranty_badge = """
            <TextView android:id="@+id/tvWarranty" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:textSize="10sp" android:textStyle="bold"
                android:paddingHorizontal="6dp" android:paddingVertical="3dp"
                android:layout_marginEnd="6dp"
                android:background="#E0E0E0" android:textColor="#424242"/>
"""

btn_advance = """
            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnAdvanceStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Advance"
                android:textSize="12sp"
                style="@style/Widget.Material3.Button.TextButton"
                android:layout_marginStart="8dp"/>
"""

content = content.replace("<TextView android:id=\"@+id/tvStatus\"", warranty_badge + "\n            <TextView android:id=\"@+id/tvStatus\"")
content = content.replace("</LinearLayout>\n        <TextView android:id=\"@+id/tvIssue\"", btn_advance + "\n        </LinearLayout>\n        <TextView android:id=\"@+id/tvIssue\"")

with open("app/src/main/res/layout/item_repair.xml", "w") as f:
    f.write(content)
