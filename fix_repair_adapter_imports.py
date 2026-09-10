with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairAdapter.kt", "r") as f:
    content = f.read()
if "ItemRepairBinding" not in content.split("class RepairAdapter")[0]:
    content = content.replace("import android.graphics.Color", "import android.graphics.Color\nimport com.shopkeeper.mobileshop.databinding.ItemRepairBinding")
with open("app/src/main/java/com/shopkeeper/mobileshop/ui/repairs/RepairAdapter.kt", "w") as f:
    f.write(content)
