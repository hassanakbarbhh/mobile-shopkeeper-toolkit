with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

btn_cash = """
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnCashClosing"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Cash Closing (Daily Reconciliation)"
            app:icon="@drawable/ic_receipt"
            app:iconSize="18dp"/>
"""

content = content.replace("<View android:layout_width=\"match_parent\" android:layout_height=\"1dp\"\n            android:background=\"@color/divider\" android:layout_marginVertical=\"16dp\"/>\n        <!-- Google & Firebase Authentication Section -->", btn_cash + "\n        <View android:layout_width=\"match_parent\" android:layout_height=\"1dp\"\n            android:background=\"@color/divider\" android:layout_marginVertical=\"16dp\"/>\n        <!-- Google & Firebase Authentication Section -->")

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
