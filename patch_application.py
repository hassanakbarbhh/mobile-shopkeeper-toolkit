with open("app/src/main/java/com/shopkeeper/mobileshop/ShopApplication.kt", "r") as f:
    content = f.read()

imports = """
import com.shopkeeper.mobileshop.utils.GlobalExceptionHandler
"""
content = content.replace("import com.google.firebase.FirebaseApp", imports + "import com.google.firebase.FirebaseApp")
content = content.replace("super.onCreate()", "super.onCreate()\n        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))")

with open("app/src/main/java/com/shopkeeper/mobileshop/ShopApplication.kt", "w") as f:
    f.write(content)
