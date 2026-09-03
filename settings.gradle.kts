@file:Suppress("UnstableApiUsage")

rootProject.name = "CodingArena"

// ---------------------------------------------------------------------------
// Target auto-detection.
//
// CodingArena ships iOS + Android targets, but the shared `:core` module is
// deliberately buildable (and testable) on any host with only a JDK. That lets
// the whole learning engine - ratings, spaced repetition, solution review - be
// unit tested in CI without an Android SDK or a Mac.
//
//   codingarena.android : build the Android target (needs the Android SDK)
//   codingarena.apple   : build the iOS targets   (needs a macOS host + Xcode)
//
// Both are auto-detected and can be forced with -P flags, e.g.
//   ./gradlew build -Pcodingarena.android=false
// ---------------------------------------------------------------------------

fun flag(name: String, autoDetected: Boolean): Boolean =
    providers.gradleProperty(name).orNull?.toBooleanStrictOrNull() ?: autoDetected

val androidSdkPresent: Boolean =
    System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        rootDir.resolve("local.properties").let { it.exists() && it.readText().contains("sdk.dir") }

val macHost: Boolean = System.getProperty("os.name").orEmpty().startsWith("Mac")

val androidEnabled = flag("codingarena.android", androidSdkPresent)
val appleEnabled = flag("codingarena.apple", macHost)

// The browser target (Kotlin/Wasm + Compose for Web). Needs no SDK or Mac, so
// it is on by default - this is how the app ships without the App Store.
val webEnabled = flag("codingarena.web", true)

// The UI module needs at least one client target to be meaningful, and its
// Compose dependencies resolve from Google's Maven repository.
val uiEnabled = flag("codingarena.ui", androidEnabled || appleEnabled || webEnabled)

// Surfaced to the project build scripts as system properties: settings.gradle
// is evaluated before any project script, so this is readable from the root
// `buildscript {}` block, which is where the Android Gradle Plugin has to be
// added to the classpath conditionally.
System.setProperty("codingarena.android", androidEnabled.toString())
System.setProperty("codingarena.apple", appleEnabled.toString())
System.setProperty("codingarena.web", webEnabled.toString())
System.setProperty("codingarena.ui", uiEnabled.toString())

pluginManagement {
    repositories {
        if (System.getProperty("codingarena.ui").toBoolean()) google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        if (System.getProperty("codingarena.ui").toBoolean()) google()
        mavenCentral()
    }
}

// `projects.core` type-safe accessors used by :composeApp
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":core")
include(":server")
if (uiEnabled) include(":composeApp")

gradle.projectsLoaded {
    logger.lifecycle(
        "CodingArena targets -> android=$androidEnabled apple=$appleEnabled web=$webEnabled ui=$uiEnabled"
    )
}
