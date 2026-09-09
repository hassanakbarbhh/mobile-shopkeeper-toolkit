with open("app/src/main/res/values-ur/strings.xml", "r") as f:
    content = f.read()

# basic translations
content = content.replace(">Mobile Shopkeeper Toolkit<", ">موبائل شاپ کیپر ٹول کٹ<")
content = content.replace(">Dashboard<", ">ڈیش بورڈ<")
content = content.replace(">New Sale (POS)<", ">نئی سیل<")
content = content.replace(">Inventory<", ">اسٹاک<")
content = content.replace(">Sales / Invoices<", ">سیلز / انوائسز<")
content = content.replace(">Repairs<", ">مرمت<")

with open("app/src/main/res/values-ur/strings.xml", "w") as f:
    f.write(content)
