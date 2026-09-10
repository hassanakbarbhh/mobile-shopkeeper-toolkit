with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

new_btn = """        <com.google.android.material.button.MaterialButton android:id="@+id/btnViewCrashLogs"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="8dp" android:text="View Crash Logs"/>
"""
content = content.replace("""        <com.google.android.material.button.MaterialButton android:id="@+id/btnAbout\"""", new_btn + """        <com.google.android.material.button.MaterialButton android:id="@+id/btnAbout\"""")

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
