with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

if "android:supportsRtl=\"true\"" not in content:
    content = content.replace("<application", "<application android:supportsRtl=\"true\"")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
