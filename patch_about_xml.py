import re

with open("app/src/main/res/layout/fragment_about.xml", "r") as f:
    content = f.read()

new_buttons = """            <!-- Rate & Share -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="8dp">
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnRateApp"
                    style="@style/Widget.Material3.Button.TonalButton"
                    android:layout_width="0dp"
                    android:layout_height="52dp"
                    android:layout_weight="1"
                    android:layout_marginEnd="6dp"
                    android:text="Rate App"
                    app:icon="@drawable/ic_heart"
                    app:cornerRadius="14dp"/>
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnShareApp"
                    style="@style/Widget.Material3.Button.TonalButton"
                    android:layout_width="0dp"
                    android:layout_height="52dp"
                    android:layout_weight="1"
                    android:layout_marginStart="6dp"
                    android:text="Share App"
                    app:icon="@drawable/ic_share"
                    app:cornerRadius="14dp"/>
            </LinearLayout>

            <!-- Privacy Policy -->
            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnPrivacyPolicy"
                style="@style/Widget.Material3.Button.OutlinedButton"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                android:text="Privacy Policy"
                app:cornerRadius="14dp"
                android:layout_marginBottom="8dp"/>

            <TextView
                android:id="@+id/tvAppVersion"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center"
                android:layout_marginTop="16dp"
                android:layout_marginBottom="24dp"
                android:text="Version"
                android:textColor="@color/text_secondary"
                android:textSize="12sp"/>"""

content = re.sub(r'            <TextView\s+android:layout_width="match_parent"\s+android:layout_height="wrap_content"\s+android:gravity="center"\s+android:layout_marginTop="16dp"\s+android:layout_marginBottom="24dp"\s+android:text="@string/version"\s+android:textColor="@color/text_secondary"\s+android:textSize="12sp"/>', new_buttons, content)

with open("app/src/main/res/layout/fragment_about.xml", "w") as f:
    f.write(content)
