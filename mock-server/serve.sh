#!/usr/bin/env bash
# 実写画像のローカル配信サーバー。既定ポート8000で mock-server/images を配信。
# アプリの参照先: Android エミュレータ=http://10.0.2.2:8000 / iOS シミュレータ=http://127.0.0.1:8000
# (imageBaseUrl の actual と一致させること)
set -euo pipefail
PORT="${1:-8000}"
DIR="$(cd "$(dirname "$0")/images" && pwd)"
echo "serving $DIR on http://0.0.0.0:$PORT"
exec python3 -m http.server "$PORT" --directory "$DIR"
