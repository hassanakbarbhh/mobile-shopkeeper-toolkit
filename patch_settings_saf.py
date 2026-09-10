with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

btn_saf = """
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnBackupData"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Backup Database (JSON) to Device"/>
            
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnRestoreData"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Restore Database (JSON) from Device"/>
"""

content = content.replace("<com.google.android.material.button.MaterialButton\n            android:id=\"@+id/btnResetDefaultStock\"", btn_saf + "\n        <com.google.android.material.button.MaterialButton\n            android:id=\"@+id/btnResetDefaultStock\"")

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
