import re

with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

# The user wants to remove the Google & Firebase Login section
content = re.sub(r'        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"\s*android:text="Google &amp; Firebase Login \(100% Free\)" android:textStyle="bold"\s*android:textColor="@color/green_900"/>.*?<View android:layout_width="match_parent" android:layout_height="1dp"\s*android:background="@color/divider" android:layout_marginVertical="16dp"/>', '', content, flags=re.DOTALL)

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
