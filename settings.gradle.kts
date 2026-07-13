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
private val testLocally = false

if (testLocally) {
  includeBuild("ContextGuard/") {
    dependencySubstitution {
      substitute(module("org.contextguard.lib:library"))
          .using(project(":library"))
          .because("Testing Context guard locally")
    }
  }
}
