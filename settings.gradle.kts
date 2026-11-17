rootProject.name = "MsgVerify"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    google()
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    google()
    mavenCentral()
    mavenLocal()
  }
}

include(":composeApp")

// Set to true to use local ContextGuard with framework extension features (HeuristicProvider, etc.)
// Set to false to use the published Maven version (extension features may not be available)
// Building locally also needs ContextGuard/gradle.properties (gitignored) containing at least
// android.useAndroidX=true.
private val testLocally = false

if (testLocally) {
  includeBuild("ContextGuard/") {
    dependencySubstitution {
      // Must match the published coordinates in gradle/libs.versions.toml.
      substitute(module("org.contextguard.lib:contextguard"))
          .using(project(":library"))
          .because("Testing Context guard locally")
    }
  }
}
