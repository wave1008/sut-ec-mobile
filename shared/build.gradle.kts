import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.sutec.mobile.shared"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_25) }
    }
    // :server が使う。指定しないとビルドした JDK の版で出力され、サーバーのイメージ(JRE 25)と食い違いうる
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_25) } }
    // iosX64 は composeApp に合わせて非宣言(CMP 1.11.0 が iosX64 非配信)。
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

