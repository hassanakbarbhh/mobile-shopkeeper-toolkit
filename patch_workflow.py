import re

with open(".github/workflows/build-apk.yml", "r") as f:
    content = f.read()

events = """on:
  push:
    branches: [ "main", "master" ]
    tags: [ "v6.*" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:
    inputs:
      release_note:
        description: 'Optional release notes for this APK build'
        required: false
        default: 'Production-ready Android APK build with twice-checking verification.'"""

content = re.sub(r'on:.*?  workflow_dispatch:.*?\n.*?\n.*?\n.*?\n', events + '\n', content, flags=re.DOTALL)

aab_job = """      - name: "Verification Check 3 of 3: Bundle Release AAB (Play Store)"
        if: startsWith(github.ref, 'refs/tags/v6.')
        run: |
          echo "=========================================================="
          echo "==> [CHECK 3/3] Executing Bundle Release AAB..."
          echo "=========================================================="
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > release.keystore
          export KEYSTORE_FILE="release.keystore"
          export KEYSTORE_PASSWORD="${{ secrets.KEYSTORE_PASSWORD }}"
          export KEY_ALIAS="${{ secrets.KEY_ALIAS }}"
          export KEY_PASSWORD="${{ secrets.KEY_PASSWORD }}"
          gradle bundleRelease --no-daemon --stacktrace
          echo "==> [CHECK 3/3 PASSED] AAB assembly completed."

      - name: "Double-Check Verification: Validate AAB Output"
        if: startsWith(github.ref, 'refs/tags/v6.')
        run: |
          AAB_FILE=$(find app/build/outputs/bundle/release/ -name "*.aab" | head -n 1)
          if [ -z "$AAB_FILE" ] || [ ! -f "$AAB_FILE" ]; then
            echo "ERROR: Double-check verification failed! AAB file was not produced."
            exit 1
          fi
          mkdir -p release_output
          cp "$AAB_FILE" "release_output/MobileShopkeeper-v6.0.0-release.aab"
          sha256sum "release_output/MobileShopkeeper-v6.0.0-release.aab" > "release_output/MobileShopkeeper-v6.0.0-release.aab.sha256"

      - name: Upload AAB as Workflow Artifact
        if: startsWith(github.ref, 'refs/tags/v6.')
        uses: actions/upload-artifact@v4
        with:
          name: MobileShopkeeper-AAB-Release
          path: release_output/MobileShopkeeper-v6.0.0-release.aab
          retention-days: 30
          
      - name: Create GitHub Release with APK"""

content = content.replace("      - name: Create GitHub Release with APK", aab_job)

with open(".github/workflows/build-apk.yml", "w") as f:
    f.write(content)
