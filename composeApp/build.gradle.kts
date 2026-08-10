import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val androidEnabled = System.getProperty("codingarena.android").toBoolean()
val appleEnabled = System.getProperty("codingarena.apple").toBoolean()

if (androidEnabled) apply(from = rootProject.file("gradle/android-app.gradle.kts"))

kotlin {
    // Desktop exists purely as a fast preview: `./gradlew :composeApp:run`
    // opens the real UI in a window in seconds, with no Xcode, no signing and
    // no device. Same Compose code as iOS, so layout and navigation bugs show
    // up here first and far more cheaply.
    jvm("desktop")

    if (appleEnabled) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "ComposeApp"
                // Compose Multiplatform requires a static framework on iOS.
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)

            implementation(libs.compose.navigation)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }

        findByName("androidMain")?.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }

        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    // `expect class DatabaseDriverFactory` triggers a beta warning otherwise.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

compose.desktop {
    application {
        mainClass = "com.codingarena.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "CodingArena"
            packageVersion = "1.0.0"
        }
    }
}
