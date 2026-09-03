import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

val androidEnabled = System.getProperty("codingarena.android").toBoolean()
val appleEnabled = System.getProperty("codingarena.apple").toBoolean()

// Adds the Android target. Must run before the `kotlin { }` block below so the
// android source sets exist by the time dependencies are wired up.
if (androidEnabled) apply(from = rootProject.file("gradle/android-core.gradle"))

kotlin {
    jvm()

    if (androidEnabled) {
        androidTarget {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    if (appleEnabled) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.koin.core)
            api(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.sqlite)
            implementation(libs.ktor.client.okhttp)
        }

        findByName("androidMain")?.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.ktor.client.okhttp)
        }

        if (appleEnabled) {
            matching { it.name == "iosMain" }.configureEach {
                dependencies {
                    implementation(libs.sqldelight.driver.native)
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}

sqldelight {
    databases {
        create("ArenaDatabase") {
            packageName.set("com.codingarena.db")
            verifyMigrations.set(true)
            // The browser has no synchronous SQLite driver - the web target runs
            // the database in a Web Worker and talks to it asynchronously. This
            // flag is global to the database, so JVM/iOS/Android also get the
            // async-generated API (`awaitAsList()` in place of `executeAsList()`).
            // On a synchronous driver those await calls simply return inline.
            generateAsync.set(true)
        }
    }
}
