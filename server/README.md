# sut-ec-mobile server

Ktor 3 + PostgreSQL(Exposed/Flyway) の EC バックエンド。設計の正本は [docs/server-design.md](../docs/server-design.md)。

## 起動

### 推奨: Apple Container(Docker デーモン不要)
Apple Silicon + macOS 15+ 向け。`brew install container` で導入(Virtualization.framework の軽量VMで
Linux コンテナを実行、Docker Desktop/デーモン不要)。ワンショット起動スクリプトを用意している:
```bash
./scripts/dev-server.sh            # Postgres(Apple Container)起動 → サーバー実行
./scripts/dev-server.sh --db-only  # DB だけ起動し接続 env を表示
```
Apple Container はコンテナ毎に IP を割り当てる(例 192.168.64.2)。スクリプトが IP を取得し
`DATABASE_URL` を組み立てる。DB データはボリューム無し=起動毎に Flyway + シードで再構築(dev 用途)。

手動の場合:
```bash
container system start --enable-kernel-install
container run -d --name sutec-db -e POSTGRES_DB=sutec -e POSTGRES_USER=sutec -e POSTGRES_PASSWORD=sutec postgres:16
container ls   # IP を確認(192.168.64.x)
DATABASE_URL=jdbc:postgresql://<IP>:5432/sutec DB_USER=sutec DB_PASSWORD=sutec ./gradlew :server:run
```

### 代替: Docker / Rancher(デーモンが起動できる環境)
```bash
docker compose up -d db          # PostgreSQL 16
./gradlew :server:run            # http://localhost:8090
# または一括:
docker compose up --build        # db + server
```

起動時に Flyway がマイグレーション(V1/V2)を適用し、カタログが空なら `CatalogSeed` を投入する。

### 動作確認
```bash
curl -s localhost:8090/health                       # {"status":"ok","db":"up"}
curl -s localhost:8090/api/v1/categories
curl -s 'localhost:8090/api/v1/products?text=earbuds&sort=PRICE_ASC&page=0&pageSize=20'
curl -s localhost:8090/api/v1/products/electronics_1
```

## 環境変数
| 変数 | 既定 | 用途 |
|---|---|---|
| `PORT` | 8090 | 待受ポート |
| `DATABASE_URL` | jdbc:postgresql://localhost:5432/sutec | JDBC URL |
| `DB_USER` / `DB_PASSWORD` | sutec / sutec | DB 資格情報 |
| `JWT_SECRET` | dev-secret-change-me | **本番は必須で変更** |
| `JWT_TTL_DAYS` | 30 | トークン有効日数 |
| `IMAGES_DIR` | mock-server/images | 静的画像ディレクトリ |
| `CORS_ORIGINS` | 未指定=anyHost | カンマ区切りで許可オリジンを限定(例 `https://app.example.com`) |
| `APP_ENV` | dev | `production` かつ `JWT_SECRET` が未設定/既定値のままだと起動失敗 |
| `RATE_LIMIT_PER_MIN` | 60 | `/auth/*`・`/me` の IP 単位レート制限(毎分) |

## テスト
- **単体**: `OrderTotalsTest`（`computeOrderTotals` の送料/税ルール、DB不要）。
- **統合**: `ApiIntegrationTest`（実 PostgreSQL に Flyway 適用+シード投入し、`/api/v1` を testApplication で通し検証。カタログ/認証(201/409/401/400)/カート/注文の権威計算/お気に入り）。DB は次の優先で選択:
  ```bash
  # 1) Testcontainers(Docker がある環境/CI): 自動で postgres:16 を起動
  ./gradlew :server:test
  # 2) 既存の外部DBを使う(例: Apple Container の Postgres)。Docker 不要
  TEST_DATABASE_URL=jdbc:postgresql://<IP>:5432/sutec ./gradlew :server:test
  # 3) Docker も TEST_DATABASE_URL も無い → 統合テストは自動スキップ(build は緑)
  ```

## 接続ポート/ホストの設定(ハードコードしない)
- **サーバー**: `PORT` 環境変数(既定 8090)。
- **アプリ(composeApp)**: `gradle.properties` の以下(または `-P` / 環境変数 `SUTEC_SERVER_*`)。ソース編集不要。
  ```
  sutec.server.host.android=10.0.2.2   # Android エミュレータ→ホスト
  sutec.server.host.ios=127.0.0.1      # iOS シミュレータ→ホスト
  sutec.server.port=8090
  ```
  例: `./gradlew :composeApp:assembleDebug -Psutec.server.port=9000`。build 時に `ServerConfigDefaults.kt` を生成し `serverBaseUrl()` が参照する。

## API 概要（`/api/v1`、詳細は server-design.md §4）
- 公開: `GET /categories` `/products`(検索/ページング) `/products/featured` `/products/by-ids` `/products/{id}` `/products/{id}/reviews` `/products/{id}/related`、`POST /auth/signup` `/auth/login`
- 要認証(Bearer JWT): `GET /me`、`/cart` 系、`/wishlist` 系、`/orders` 系、`/addresses`・`/payment-methods` 系
- 画像: `GET /images/<id>-<n>.jpg`
- 注文確定 `POST /orders` は **サーバーがカートから金額を権威計算**（クライアント送信の金額は受けない）。

## 注意
- スキーマの単一情報源は Flyway(`src/main/resources/db/migration`)。Exposed の `db/Tables.kt` はこれに一致させる。
- カタログシード `seed/CatalogSeed.kt` がカタログの唯一の情報源(第1段階の client `MockCatalog.kt` から移植。client 側の mock は削除済み)。画像は相対パス。
- 住所/支払い/注文はユーザー単位で空スタート(実認証のため。モックのグローバル seed は持たない)。
