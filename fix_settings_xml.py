with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()
import re
new_btn = """        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnCashClosing"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Cash Closing (Daily Reconciliation)"
            app:icon="@drawable/ic_receipt"
            app:iconSize="18dp"/>
        
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnManageSellersSettings"
"""
content = content.replace('<com.google.android.material.button.MaterialButton\n            android:id="@+id/btnManageSellersSettings"', new_btn)
with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
