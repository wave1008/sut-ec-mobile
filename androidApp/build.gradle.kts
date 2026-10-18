// Android アプリ本体(AGP 9 は KMP のモジュールに com.android.application を同居させられないので分けている)。
// 画面と処理は :composeApp(KMP)にあり、ここは Activity・Application・マニフェスト・テーマだけを持つ。
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.sutec.mobile"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    // BuildConfig.DEBUG を SutEcApplication が :composeApp へ渡す(NavReset.android.kt)。
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.sutec.mobile.android"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
}
