import re

with open(".github/workflows/build-apk.yml", "r") as f:
    content = f.read()

# Fix duplicates in workflow_dispatch
bad_inputs = """      release_note:
        description: 'Optional release notes for this APK build'
        required: false
        default: 'Production-ready Android APK build with twice-checking verification.'
        required: false
        default: 'Production-ready Android APK build with twice-checking verification.'"""
good_inputs = """      release_note:
        description: 'Optional release notes for this APK build'
        required: false
        default: 'Production-ready Android APK build with twice-checking verification.'"""
content = content.replace(bad_inputs, good_inputs)

# Update Release step to be dynamic and include the AAB
bad_release = """      - name: Create GitHub Release with APK
        if: github.event_name == 'workflow_dispatch' || startsWith(github.ref, 'refs/tags/')
        continue-on-error: true
        uses: softprops/action-gh-release@v2
        with:
          files: |
            release_output/MobileShopkeeper-v1.0.0-debug.apk
            release_output/MobileShopkeeper-v1.0.0-debug.apk.sha256
          tag_name: v1.0.0-build-${{ github.run_number }}
          name: "Mobile Shopkeeper POS v1.0.0 (Build #${{ github.run_number }})"
          body: |
            ### 📱 Mobile Shopkeeper Point of Sale - APK Release
            **Build Number:** #${{ github.run_number }}
            **Verification:** Passed Twice-Checking Protocol (Check 1: Unit Tests & Code Integrity, Check 2: Full Assembly & Integrity Audit)
            
            #### 📦 Download & Install:
            - **APK Binary:** `MobileShopkeeper-v1.0.0-debug.apk`
            - **Checksum:** See attached `.sha256` file
            
            ${{ github.event.inputs.release_note || 'Production-ready Android APK with modern auth, thermal printing, and multi-role POS.' }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}"""

good_release = """      - name: Create GitHub Release with APK/AAB
        if: github.event_name == 'workflow_dispatch' || startsWith(github.ref, 'refs/tags/')
        continue-on-error: true
        uses: softprops/action-gh-release@v2
        with:
          files: |
            release_output/MobileShopkeeper-v1.0.0-debug.apk
            release_output/MobileShopkeeper-v1.0.0-debug.apk.sha256
            release_output/MobileShopkeeper-v6.0.0-release.aab
            release_output/MobileShopkeeper-v6.0.0-release.aab.sha256
          name: "Mobile Shopkeeper POS Build #${{ github.run_number }}"
          body: |
            ### 📱 Mobile Shopkeeper Point of Sale - APK/AAB Release
            **Build Number:** #${{ github.run_number }}
            **Verification:** Passed Twice-Checking Protocol (Check 1: Unit Tests & Code Integrity, Check 2: Full Assembly & Integrity Audit)
            
            #### 📦 Download & Install:
            - **APK Binary:** `MobileShopkeeper-v1.0.0-debug.apk`
            - **AAB Bundle:** `MobileShopkeeper-v6.0.0-release.aab`
            
            ${{ github.event.inputs.release_note || 'Production-ready Android APK with modern auth, thermal printing, and multi-role POS.' }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}"""

content = content.replace(bad_release, good_release)

with open(".github/workflows/build-apk.yml", "w") as f:
    f.write(content)
