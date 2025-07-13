import com.android.build.api.dsl.androidLibrary
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.gradle.signing") // keep this for signing for local maven repository
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
                // ktor client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)  // ktor's kotlinx.serialization integration

                // kotlinx.serialization
                implementation(libs.kotlinx.serialization.json) // the core JSON serialization library

                // pytorch
                implementation(libs.pytorch.lite.multiplatform)

                // for resources
                implementation(compose.runtime)
                implementation(compose.components.resources)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)

            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.pytorch.lite.multiplatform)
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
                implementation(libs.pytorch.lite.multiplatform)
            }
        }
    }
}

android {
    namespace = "org.contextguard.lib"
    compileSdk = 36
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    coordinates("org.contextguard.lib", "ContextGuard", "1.0.0")

    pom {
        name = "ContextGuard"
        description = "Kotlin Multiplatform library"
        url = "github url" //todo

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "" //todo
                name = "" //todo
                email = "" //todo
            }
        }

        scm {
            url = "github url" //todo
        }
    }
    if (project.hasProperty("signing.keyId")) signAllPublications()
}

configure<SigningExtension> {
    useGpgCmd()
    sign(publishing.publications)
}