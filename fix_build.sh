sed -i 's/storePassword = System.getenv("STORE_PASSWORD")//g' app/build.gradle.kts
sed -i 's/keyAlias = "upload"//g' app/build.gradle.kts
sed -i 's/signingConfig = signingConfigs.getByName("release")//g' app/build.gradle.kts
