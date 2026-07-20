import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// サーバー接続先はハードコードせず設定値から生成。優先順: -P/gradle.properties(sutec.server.*)
// → 環境変数(SUTEC_SERVER_*) → 既定。ServerConfigDefaults.kt を commonMain へ生成し actual が参照する。
val serverHostAndroid: String = providers.gradleProperty("sutec.server.host.android")
    .orElse(providers.environmentVariable("SUTEC_SERVER_HOST_ANDROID")).getOrElse("10.0.2.2")
val serverHostIos: String = providers.gradleProperty("sutec.server.host.ios")
    .orElse(providers.environmentVariable("SUTEC_SERVER_HOST_IOS")).getOrElse("127.0.0.1")
val serverPort: String = providers.gradleProperty("sutec.server.port")
    .orElse(providers.environmentVariable("SUTEC_SERVER_PORT")).getOrElse("8090")

val serverConfigDir = layout.buildDirectory.dir("generated/serverconfig/kotlin")
val generateServerConfig = tasks.register("generateServerConfig") {
    val out = serverConfigDir
    inputs.property("hostAndroid", serverHostAndroid)
    inputs.property("hostIos", serverHostIos)
    inputs.property("port", serverPort)
    outputs.dir(out)
    doLast {
        val pkg = out.get().dir("com/sutec/mobile/data/remote").asFile
        pkg.mkdirs()
        pkg.resolve("ServerConfigDefaults.kt").writeText(
            """
            package com.sutec.mobile.data.remote

            // 生成物。編集しない。値は gradle.properties(sutec.server.*) / -P / 環境変数(SUTEC_SERVER_*)。
            internal object ServerConfigDefaults {
                const val ANDROID_HOST: String = "$serverHostAndroid"
                const val IOS_HOST: String = "$serverHostIos"
                const val PORT: String = "$serverPort"
            }
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // iosX64(Intel Mac シミュレータ)は Compose Multiplatform 1.11.0 が非配信のため宣言しない。
    // 実機=iosArm64 / Apple Silicon シミュレータ=iosSimulatorArm64 で iosMain は成立する。
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateServerConfig)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(projects.shared)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.multiplatform.settings.no.arg)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.sutec.mobile"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    // BuildConfig.DEBUG を使う(NavReset.android.kt: デバッグ起動でナビをルート正規化)。
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.sutec.mobile"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
