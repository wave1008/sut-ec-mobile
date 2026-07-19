#!/usr/bin/env bash
# Docker デーモン不要のローカル開発。Apple Container(Virtualization.framework の軽量VM)で
# PostgreSQL を起動し、DATABASE_URL をコンテナIPに向けてサーバーを実行する。
# 使い方: scripts/dev-server.sh [--db-only]
#   --db-only : DB だけ起動し、接続用 env を表示して終了(サーバーは各自 ./gradlew :server:run)
# 前提: `brew install container`(Apple Container CLI, macOS 15+/Apple Silicon)。
set -euo pipefail

export PATH="/opt/homebrew/bin:$PATH"
NAME=sutec-db
IMAGE=postgres:16

if ! command -v container >/dev/null 2>&1; then
  echo "Apple Container 未インストール。'brew install container' を実行してください。" >&2
  exit 1
fi

# apiserver 起動(冪等)。初回はカーネルを自動インストール。
container system start --enable-kernel-install >/dev/null 2>&1 || true

# postgres コンテナを冪等起動(存在すれば start、無ければ run)。データはボリューム無し=起動毎に
# Flyway マイグレーション + カタログシードで再構築される(dev 用途。永続化が要るなら -v を足す)。
if container inspect "$NAME" >/dev/null 2>&1; then
  container start "$NAME" >/dev/null 2>&1 || true
else
  container run -d --name "$NAME" \
    -e POSTGRES_DB=sutec -e POSTGRES_USER=sutec -e POSTGRES_PASSWORD=sutec \
    "$IMAGE" >/dev/null
fi

# 起動待ち(postgres が接続受付開始するまで)。
for _ in $(seq 1 30); do
  if container logs "$NAME" 2>/dev/null | grep -q "database system is ready to accept connections"; then break; fi
  sleep 1
done

IP=$(container inspect "$NAME" | python3 -c \
  'import sys,json;c=json.load(sys.stdin);c=c[0] if isinstance(c,list) else c;print(c["status"]["networks"][0]["ipv4Address"].split("/")[0])')

export DATABASE_URL="jdbc:postgresql://${IP}:5432/sutec"
export DB_USER=sutec DB_PASSWORD=sutec
export JWT_SECRET="${JWT_SECRET:-dev-secret-change-me}"
export IMAGES_DIR="$(cd "$(dirname "$0")/.." && pwd)/mock-server/images"

echo "PostgreSQL(Apple Container) IP=${IP}"
echo "DATABASE_URL=${DATABASE_URL}"

if [ "${1:-}" = "--db-only" ]; then
  echo "DB のみ起動。サーバー実行例:"
  echo "  DATABASE_URL='${DATABASE_URL}' DB_USER=sutec DB_PASSWORD=sutec IMAGES_DIR='${IMAGES_DIR}' ./gradlew :server:run"
  exit 0
fi

cd "$(dirname "$0")/.."
exec ./gradlew :server:run --console=plain
