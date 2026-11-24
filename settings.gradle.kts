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

// Firebase Test Lab driver for the Android benchmark (see benchmark/testlab/README.md).
include(":benchmarkTestLab")
project(":benchmarkTestLab").projectDir = file("benchmark/testlab/android")

// Set to true to use local ContextGuard with framework extension features (HeuristicProvider, etc.)
// Set to false to use the published Maven version (extension features may not be available)
// Building locally also needs ContextGuard/gradle.properties (gitignored) containing at least
// android.useAndroidX=true.
private val testLocally = false
// benchmark/testlab/build_*.sh pass -PmsgverifyTestLocally=true (or the matching
// ORG_GRADLE_PROJECT_ environment variable for the Xcode build phase), so the Test Lab builds
// always contain the ContextGuard source of this repository.
private val buildContextGuardFromSource =
    testLocally || providers.gradleProperty("msgverifyTestLocally").orNull == "true"

if (buildContextGuardFromSource) {
  includeBuild("ContextGuard/") {
    dependencySubstitution {
      // Must match the published coordinates in gradle/libs.versions.toml.
      substitute(module("org.contextguard.lib:contextguard"))
          .using(project(":library"))
          .because("Testing Context guard locally")
    }
  }
}
