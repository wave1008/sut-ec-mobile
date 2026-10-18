plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.sutec.mobile.server.ApplicationKt")
}

dependencies {
    implementation(projects.shared)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.rate.limit)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.json)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.logback.classic)
    implementation(libs.jbcrypt)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
}

// 版を名指しする jvmToolchain は「その版の JDK が手元にあること」を要るので使わず、出力の版だけ揃える(JDK 25 以降でビルドできる)。
// 実行環境は server/Dockerfile の JRE 25。
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25) } }
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<Test> {
    useJUnitPlatform()
    // 統合テストは Docker(Testcontainers) か TEST_DATABASE_URL の外部DBが要る。
    // どちらも無ければ ApiIntegrationTest は assumeTrue でスキップ(build は緑のまま)。
    System.getenv("TEST_DATABASE_URL")?.let { environment("TEST_DATABASE_URL", it) }
    testLogging { events("passed", "skipped", "failed") }
}
