import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val androidEnabled = System.getProperty("codingarena.android").toBoolean()
val appleEnabled = System.getProperty("codingarena.apple").toBoolean()
val webEnabled = System.getProperty("codingarena.web").toBoolean()

if (androidEnabled) apply(from = rootProject.file("gradle/android-app.gradle"))

kotlin {
    // Desktop exists purely as a fast preview: `./gradlew :composeApp:run`
    // opens the real UI in a window in seconds, with no Xcode, no signing and
    // no device. Same Compose code as iOS, so layout and navigation bugs show
    // up here first and far more cheaply.
    jvm("desktop")

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
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "ComposeApp"
                // Compose Multiplatform requires a static framework on iOS.
                isStatic = true
            }
        }
    }

    if (webEnabled) {
        // Kotlin/Wasm + Compose for Web. The canvas-based UI runs the exact same
        // commonMain Compose code as iOS. Output: composeApp/build/dist/wasmJs/
        // productionExecutable/ - static files, deployable to any host.
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("codingarena")
            browser {
                commonWebpackConfig {
                    outputFileName = "codingarena.js"
                }
            }
            binaries.executable()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.animation)
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

        if (webEnabled) {
            getByName("wasmJsMain").dependencies {
                implementation(libs.kotlinx.browser)
                // sql.js (SQLite/Wasm) for the database Web Worker, plus the
                // plugin that copies its files next to the bundle. See
                // composeApp/webpack.config.d/sqljs.js and
                // src/wasmJsMain/resources/sqljs.worker.js.
                implementation(npm("sql.js", "1.8.0"))
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            }
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

        // Gradle/Kotlin still build reliably on JDK 19, while the packaged app
        // can use a newer runtime on newer macOS releases. Override with:
        // -Pcodingarena.desktopJavaHome=/path/to/jdk
        providers.gradleProperty("codingarena.desktopJavaHome").orNull?.let {
            javaHome = it
        }

        // NOTE: do not add -XstartOnFirstThread here. That flag is for GLFW/LWJGL
        // apps that drive their own event loop; Compose Desktop is AWT/Swing, and
        // AWT wants to start AppKit on the main thread itself. With the flag set,
        // main is parked in Compose's runBlocking, AppKit's run loop never starts,
        // and the AWT event thread hangs forever inside
        // MTLGraphicsConfig.tryLoadMetalLibrary - the process stays alive with no
        // window and no Dock icon.

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "CodingArena"
            packageVersion = "1.0.0"
        }
    }
}
