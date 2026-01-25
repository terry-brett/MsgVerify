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
