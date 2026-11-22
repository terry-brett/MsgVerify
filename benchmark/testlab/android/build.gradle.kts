import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Firebase Test Lab driver for the Android benchmark (see ../README.md).
//
// Test Lab cannot run benchmark/run_android.sh, which needs adb on the host. This instrumentation
// test does the same steps on the device instead. It instruments only itself
// ("self-instrumenting"), so it runs in its own process and the measured app is the unchanged,
// non-debuggable "benchmark" build of :composeApp in a process of its own.
plugins {
  alias(libs.plugins.androidTest)
  alias(libs.plugins.kotlinAndroid)
}

android {
  namespace = "com.terrydroid.msgverify.testlab"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    // Matches the app's "benchmark" build type. Only the driver APK is debuggable.
    create("benchmark") {
      isDebuggable = true
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
    }
  }

  targetProjectPath = ":composeApp"
  experimentalProperties["android.experimental.self-instrumenting"] = true

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

dependencies {
  implementation(libs.androidx.testExt.junit)
  implementation(libs.androidx.runner)
  implementation(libs.junit)
}

androidComponents { beforeVariants(selector().all()) { it.enable = it.buildType == "benchmark" } }
