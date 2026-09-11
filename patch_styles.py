import re

with open("app/src/main/res/values/themes.xml", "r") as f:
    content = f.read()

# Add window animations
if "<item name=\"android:windowAnimationStyle\">@style/WindowAnimations</item>" not in content:
    content = content.replace("</style>", "    <item name=\"android:windowAnimationStyle\">@style/WindowAnimations</item>\n    </style>", 1)
    
    anim_style = """
    <style name="WindowAnimations">
        <item name="android:activityOpenEnterAnimation">@android:anim/fade_in</item>
        <item name="android:activityOpenExitAnimation">@android:anim/fade_out</item>
        <item name="android:activityCloseEnterAnimation">@android:anim/fade_in</item>
        <item name="android:activityCloseExitAnimation">@android:anim/fade_out</item>
    </style>
</resources>"""
    content = content.replace("</resources>", anim_style)

    with open("app/src/main/res/values/themes.xml", "w") as f:
        f.write(content)
