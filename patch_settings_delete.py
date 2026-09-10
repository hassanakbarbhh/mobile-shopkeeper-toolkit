import re

with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

delete_btn = """        <com.google.android.material.button.MaterialButton android:id="@+id/btnDeleteAccount"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textColor="#D32F2F"
            app:iconTint="#D32F2F"
            app:backgroundTint="#FFEBEE"
            android:text="Delete my account &amp; cloud data"
            app:icon="@drawable/ic_delete"
            app:iconSize="18dp"/>
        
        <TextView"""

content = content.replace('        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"\n            android:gravity="center" android:layout_marginTop="24dp"', delete_btn + ' android:layout_width="match_parent" android:layout_height="wrap_content"\n            android:gravity="center" android:layout_marginTop="24dp"')

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
