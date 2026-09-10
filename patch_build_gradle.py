import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('versionCode = 6', 'versionCode = 7')
content = content.replace('versionName = "6.0"', 'versionName = "6.0.0"')

signing_config = """  signingConfigs {
    create("release") {
      val keyFile = System.getenv("KEYSTORE_FILE") ?: project.findProperty("KEYSTORE_FILE")?.toString() ?: ""
      if (keyFile.isNotEmpty() && file(keyFile).exists()) {
        storeFile = file(keyFile)
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD")?.toString()
        keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS")?.toString()
        keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD")?.toString()
      } else {
        storeFile = getByName("debug").storeFile
        storePassword = getByName("debug").storePassword
        keyAlias = getByName("debug").keyAlias
        keyPassword = getByName("debug").keyPassword
      }
    }
  }

  buildTypes {"""

content = content.replace('  buildTypes {', signing_config)

release_type = """    release {
      signingConfig = signingConfigs.getByName("release")
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }"""

content = re.sub(r'    release \{.*?\n    \}', release_type, content, flags=re.DOTALL)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
