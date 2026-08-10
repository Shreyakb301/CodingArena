// Applied to :core only when an Android SDK is present. Kept in a script
// plugin so that build scripts never reference Android Gradle Plugin types on
// machines where the plugin is not on the classpath.
import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

apply(plugin = "com.android.library")

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }
}

extensions.configure<LibraryExtension>("android") {
    namespace = "com.codingarena.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
