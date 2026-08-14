// The Android Gradle Plugin is only put on the buildscript classpath when an
// Android SDK is actually available. Without this guard a JDK-only machine
// cannot even configure the build, which would make the shared learning engine
// untestable in CI. See settings.gradle.kts for where these flags come from.
buildscript {
    val androidEnabled = System.getProperty("codingarena.android").toBoolean()
    repositories {
        if (androidEnabled) google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        if (androidEnabled) classpath("com.android.tools.build:gradle:8.7.3")
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
}

tasks.register("checkCore") {
    group = "verification"
    description = "Compiles and unit-tests the shared learning engine on the JVM only."
    dependsOn(":core:jvmTest")
}
