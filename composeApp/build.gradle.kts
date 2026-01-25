import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

val generatedCommonResourcesDir = layout.buildDirectory.dir("generated/commonMain/resources")
val composeResourcesDir = layout.projectDirectory.dir("src/commonMain/composeResources")

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.androidx.activity.compose)
      implementation(libs.koin.core)
      implementation(libs.koin.android)
    }
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodel)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.material.icons)
      implementation(libs.context.guard)

      implementation(libs.kotlinx.serialization.json)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    val commonMain by getting { resources.srcDir(generatedCommonResourcesDir) }
  }
}

android {
  namespace = "com.terrydroid.msgverify"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.terrydroid.msgverify"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  buildTypes { getByName("release") { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies { debugImplementation(compose.uiTooling) }

val syncMocksToCommonResources by
    tasks.registering(Sync::class) {
      from(rootProject.file("mocks"))
      into(composeResourcesDir.dir("files/mocks"))
      include("**/*.json")
    }

// Declare dependency for copyNonXmlValueResourcesForCommonMain
tasks.named("copyNonXmlValueResourcesForCommonMain") {
  dependsOn(syncMocksToCommonResources)
}

// Ensure resources are there before any resource processing
tasks
    .matching {
      it.name.contains("process", ignoreCase = true) &&
          it.name.contains("Resources", ignoreCase = true)
    }
    .configureEach { dependsOn(syncMocksToCommonResources) }
