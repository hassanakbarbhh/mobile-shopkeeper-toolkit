import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.shopkeeper.mobileshop"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.shopkeeper.mobileshop"
    minSdk = 26
    targetSdk = 36
    versionCode = 7
    versionName = "6.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    getByName("debug") {
      val rootDebugKeystore = rootProject.file("debug.keystore")
      if (rootDebugKeystore.exists()) {
        storeFile = rootDebugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("release") {
      val keyFile = System.getenv("KEYSTORE_FILE") ?: project.findProperty("KEYSTORE_FILE")?.toString() ?: ""
      val storePass = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD")?.toString() ?: ""
      val keyAliasVal = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS")?.toString() ?: ""
      val keyPass = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD")?.toString() ?: ""
      if (keyFile.isNotEmpty() && file(keyFile).exists() &&
          storePass.isNotEmpty() && keyAliasVal.isNotEmpty() && keyPass.isNotEmpty()
      ) {
        storeFile = file(keyFile)
        storePassword = storePass
        keyAlias = keyAliasVal
        keyPassword = keyPass
      }
      // SECURITY (BUG-006): no silent fallback to the debug keystore.
      // If release credentials are absent, the release signing config stays
      // unset and assembleRelease FAILS with a clear signing error instead of
      // producing a production APK signed with the well-known debug key.
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = "secrets.defaults.properties"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices {
  missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

dependencies {
    // implementation(libs.itext7.core) // iText is paid/AGPL, let's use Android's native PdfDocument for free
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.cardview)
  implementation(libs.androidx.coordinatorlayout)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.biometric)
  implementation(libs.mpandroidchart)
  implementation(libs.play.services.auth)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.database)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.analytics)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  "ksp"(libs.androidx.room.compiler)
}
