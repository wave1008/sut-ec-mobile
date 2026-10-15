package com.sutec.mobile.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.sutec.mobile.server.seed.SeedRunner
import org.flywaydb.core.Flyway
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database as ExposedDatabase

// DATABASE_URL/DB_USER/DB_PASSWORD は システムプロパティ → 環境変数 → 既定 の順で解決
// (テストは System.setProperty で注入する)。Flyway がスキーマの正本。Exposed の SchemaUtils.create は使わない。
object Database {
    private const val DB_CONNECT_WAIT_MS = 60_000L

    lateinit var dataSource: DataSource
        private set

    private fun cfg(key: String, default: String): String =
        System.getProperty(key) ?: System.getenv(key) ?: default

    // 冪等: 複数回呼ばれても一度だけ初期化する(テストで module() が繰り返し走るため)。
    fun init() {
        if (::dataSource.isInitialized) return
        val url = cfg("DATABASE_URL", "jdbc:postgresql://localhost:5432/sutec")
        val user = cfg("DB_USER", "sutec")
        val password = cfg("DB_PASSWORD", "sutec")

        val ds = HikariDataSource(HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            // DB と同時に起動されても落ちないよう、最初の接続が通るまで待つ(compose で DB の healthy を待たずに起動するため)。
            // 単位 ms。PostgreSQL の初回起動は数秒なので 60 秒は十分な余裕。尽きたら従来どおり起動に失敗する。
            initializationFailTimeout = DB_CONNECT_WAIT_MS
        })
        dataSource = ds
        Flyway.configure().dataSource(ds).load().migrate()
        ExposedDatabase.connect(ds)
        SeedRunner.seedIfEmpty()
    }
}
