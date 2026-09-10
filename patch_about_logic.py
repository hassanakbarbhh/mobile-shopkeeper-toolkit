import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/about/AboutFragment.kt", "r") as f:
    content = f.read()

import_str = """import com.shopkeeper.mobileshop.BuildConfig"""
content = content.replace("import com.shopkeeper.mobileshop.R", import_str + "\nimport com.shopkeeper.mobileshop.R")

bind_btn = """        binding.btnEmail.setOnClickListener { openUrl("mailto:hassanakbarbhh@gmail.com") }

        binding.tvAppVersion.text = "Version " + BuildConfig.VERSION_NAME

        val appUrl = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        binding.btnRateApp.setOnClickListener { openUrl(appUrl) }
        binding.btnShareApp.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mobile Shopkeeper Toolkit")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Manage your mobile shop efficiently with this app: " + appUrl)
            startActivity(Intent.createChooser(shareIntent, "Share App"))
        }
        binding.btnPrivacyPolicy.setOnClickListener { openUrl("https://hassanakbarbhh.github.io/mobile-shopkeeper-toolkit/privacy-policy.html") }"""

content = content.replace("        binding.btnEmail.setOnClickListener { openUrl(\"mailto:hassanakbarbhh@gmail.com\") }", bind_btn)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/about/AboutFragment.kt", "w") as f:
    f.write(content)
