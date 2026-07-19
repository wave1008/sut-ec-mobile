# sut-ec-mobile

EC 買い物アプリ。**Compose Multiplatform** で iOS / Android 両対応。
自動テストツールをテストするためテスト対象として使用。

クライアント（`composeApp`）は Ktor 3 + PostgreSQL の実サーバー（`:server`）に接続して動作する。
モデル・注文金額計算などの契約は `:shared` が単一情報源。設計の正本は [docs/server-design.md](docs/server-design.md)（第1段階のモック設計は [docs/design.md](docs/design.md)、現在は歴史的記録）。

## モジュール

- `:composeApp` — Android / iOS アプリ本体
- `:shared` — モデル・DTO・注文金額計算など client/server 共通の契約
- `:server` — Ktor 3 + PostgreSQL バックエンド（詳細は [server/README.md](server/README.md)）

## クイックスタート

```bash
./scripts/dev-server.sh              # Postgres(Apple Container) + サーバー起動 (http://localhost:8090)
./gradlew :composeApp:assembleDebug  # Android デバッグビルド
```

サーバー起動の詳細（Docker Compose 代替、環境変数、テスト）は [server/README.md](server/README.md) を参照。

## License

本リポジトリのソースコードは [MIT License](LICENSE)（Copyright (c) 2026 wave1008）。
ただし `mock-server/images/` の商品画像は対象外で、別途 [mock-server/image-provenance.json](mock-server/image-provenance.json) の出典・ライセンス記録に従う。
