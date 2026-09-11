import re

with open("app/src/main/res/layout/fragment_settings.xml", "r") as f:
    content = f.read()

# Remove the broken btnBackupData
content = re.sub(r'\s*<com\.google\.android\.material\.button\.MaterialButton\s*android:id="@\+id/btnBackupData".*?/>', '', content, flags=re.DOTALL)

# Rename the section text
content = content.replace("Secure Database Backup (Google Drive / Local)", "Manual Database Export & Restore")
content = content.replace("Export Backup", "Export Database File")
content = content.replace("Import Backup", "Restore from File")

with open("app/src/main/res/layout/fragment_settings.xml", "w") as f:
    f.write(content)
