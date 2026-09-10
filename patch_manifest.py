import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

permissions_replacement = """    <!-- Required for Firebase cloud sync and online catalog operations -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Required to check connectivity before attempting cloud sync -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Required for secure fingerprint/face unlock to access the app -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    
    <!-- Required for sending local reminders for pending repairs and dues -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <!-- Required for connecting to and printing receipts via thermal POS printers -->
    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />"""

content = re.sub(r'    <uses-permission android:name="android\.permission\.INTERNET" />.*<uses-permission android:name="android\.permission\.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />', permissions_replacement, content, flags=re.DOTALL)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
