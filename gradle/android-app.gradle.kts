// Applied to :composeApp only when an Android SDK is present. See
// gradle/android-core.gradle.kts for why this lives in a script plugin.
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

apply(plugin = "com.android.application")

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }
}

extensions.configure<BaseAppModuleExtension>("android") {
    namespace = "com.codingarena"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codingarena"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
