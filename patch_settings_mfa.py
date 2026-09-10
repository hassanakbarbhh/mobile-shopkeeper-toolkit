with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

new_btn = """                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnManageGoogleAuth"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Manage Google &amp; Firebase Login"
                    android:textSize="12sp"
                    app:icon="@drawable/ic_google"
                    app:iconSize="16dp"
                    app:iconTint="@null"/>
                
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnSetupMfa"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="6dp"
                    android:text="Setup 2FA (Google Authenticator)"
                    android:textSize="12sp"
                    app:icon="@drawable/ic_lock"
                    app:iconSize="16dp"/>
"""
content = content.replace("""                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnManageGoogleAuth"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Manage Google &amp; Firebase Login"
                    android:textSize="12sp"
                    app:icon="@drawable/ic_google"
                    app:iconSize="16dp"
                    app:iconTint="@null"/>""", new_btn)

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
