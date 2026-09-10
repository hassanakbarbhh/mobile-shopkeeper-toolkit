with open("app/src/main/res/layout/fragment_dues.xml", "r") as f:
    content = f.read()

content = content.replace('xmlns:android="http://schemas.android.com/apk/res/android"', 'xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:app="http://schemas.android.com/apk/res-auto"')

with open("app/src/main/res/layout/fragment_dues.xml", "w") as f:
    f.write(content)
