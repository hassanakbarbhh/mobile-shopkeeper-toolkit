with open("gradle/libs.versions.toml", "r") as f:
    code = f.read()

code = code.replace('[versions]\n', '[versions]\nitea = "7.2.3"\n')
code = code.replace('[libraries]\n', '[libraries]\nitext7-core = { group = "com.itextpdf", name = "itext7-core", version.ref = "itea" }\n')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(code)

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = code.replace('dependencies {\n', 'dependencies {\n    // implementation(libs.itext7.core) // iText is paid/AGPL, let\'s use Android\'s native PdfDocument for free\n')

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
