with open("gradle/libs.versions.toml", "r") as f:
    code = f.read()
    
# Add ZXing library version and dependency
code = code.replace('[versions]\n', '[versions]\nzxing = "3.5.3"\nzxingAndroid = "4.3.0"\n')
code = code.replace('[libraries]\n', '[libraries]\nzxing-core = { group = "com.google.zxing", name = "core", version.ref = "zxing" }\nzxing-android-embedded = { group = "com.journeyapps", name = "zxing-android-embedded", version.ref = "zxingAndroid" }\n')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(code)

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = code.replace('dependencies {\n', 'dependencies {\n    implementation(libs.zxing.core)\n    implementation(libs.zxing.android.embedded)\n')

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
