import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "org.contextguard.lib"
version = "1.0.0"
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // ktor (URL parsing)
                implementation(libs.ktor.client.core)

                // for resources
                implementation(compose.runtime)
                implementation(compose.components.resources)

                // ktensorflow
                implementation(libs.ktensorflow.core)

                // spell check
                implementation(libs.platform.spellchecker)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)

            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }
        // Link the platform-specific iOS source sets to iosMain
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }
    }
}

android {
    namespace = "org.contextguard.lib"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

// Android instrumented test dependencies
dependencies {
    add("androidTestImplementation", kotlin("test"))
    add("androidTestImplementation", "androidx.test.ext:junit:1.1.5")
    add("androidTestImplementation", "androidx.test:runner:1.5.2")
    add("androidTestImplementation", "androidx.test:core:1.5.0")
    add("androidTestImplementation", "androidx.test:rules:1.5.0")
    add("androidTestImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    add("androidTestImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    add("androidTestImplementation", "androidx.multidex:multidex:2.0.1")
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    coordinates(
        groupId = "org.contextguard.lib",
        artifactId = "contextguard",
        version = "1.0.0"
    )

    pom {
        name = "ContextGuard"
        description = "Kotlin Multiplatform library"
        url = "https://github.com/terry-brett/MsgVerify"

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "contextguard"
                name = "Terry Brett, Yash Soni, Odin Asbjørnsen"
                email = "terry.marcin.brett@dnb.no"
            }
        }

        scm {
            url = "https://github.com/terry-brett/MsgVerify"
        }
    }

    signAllPublications()
}

task("testClasses") {}