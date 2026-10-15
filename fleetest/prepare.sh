#!/bin/bash
# fleetest のクイックスタート用に、sut-ec-mobile のサーバーを動かす準備をする。fleetest の作業フォルダで実行する:
#   bash ../sut-ec-mobile/fleetest/prepare.sh [テストプロジェクト名(既定 project1)]
# 段: ①Mac の確認 ②Apple Container(無ければ Homebrew で入れる)③ランタイムの起動 ④イメージの取得 ⑤compose.yaml の配置。
# 済んでいる段は飛ばす(何度実行してもよい)。シェルの設定ファイルは変更しない。アプリはビルドしない。
# 取得するイメージは compose.yaml の image: 行から読む(イメージ名の正本は compose.yaml の1か所)。
# 取得は linux/arm64 に絞る: 絞らないと Apple Container は全プラットフォームを取得・展開する(postgres:16-alpine で 53 秒 → 8 秒)。
set -euo pipefail

PROJECT="${1:-project1}"
HERE="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_SRC="$HERE/compose.yaml"
# 非対話のシェルでは Homebrew の bin が PATH に無いことがある(このプロセスの中だけで足す)
export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"

step() { printf '\n[%s/5] %s\n' "$1" "$2"; }
fail() { printf 'ERROR: %s\n' "$1" >&2; exit 1; }

step 1 "Checking this Mac and the work folder"
[ "$(uname -s)" = "Darwin" ] && [ "$(uname -m)" = "arm64" ] || fail "this needs an Apple Silicon Mac"
[ -f Package.swift ] || fail "run this in your fleetest work folder (the folder with Package.swift), not in $(pwd)"
[ -f "$COMPOSE_SRC" ] || fail "missing $COMPOSE_SRC"
case "$PROJECT" in *[!A-Za-z0-9_-]*|"") fail "invalid project name: $PROJECT (letters, digits, _ and - only)";; esac
echo "OK: $(pwd)"

step 2 "Apple Container"
if command -v container >/dev/null 2>&1; then
  echo "OK: $(container --version)"
else
  command -v brew >/dev/null 2>&1 || fail "Homebrew is not installed. Install it from https://brew.sh and run this again"
  echo "Installing Apple Container with Homebrew..."
  brew install container
fi

step 3 "Starting the container runtime"
if container system status >/dev/null 2>&1; then
  echo "OK: already running"
else
  # 初回は Linux カーネルを取得する(数百 MB)
  container system start --enable-kernel-install
fi

step 4 "Pulling the images"
images=$(sed -n 's/^[[:space:]]*image:[[:space:]]*//p' "$COMPOSE_SRC")
[ -n "$images" ] || fail "no image: lines in $COMPOSE_SRC"
for image in $images; do
  echo "Pulling $image ..."
  container image pull --platform linux/arm64 "$image"
done

step 5 "Declaring the server to fleetest"
if [ ! -d "TestProjects/$PROJECT" ]; then
  FLEETEST="${FLEETEST:-../foundation-tester/.build/debug/fleetest}"
  [ -x "$FLEETEST" ] || fail "fleetest not found at $FLEETEST (install fleetest first, or set FLEETEST to its path)"
  echo "Creating the test project $PROJECT ..."
  "$FLEETEST" project create "$PROJECT"
fi
DEST_DIR="TestProjects/$PROJECT/workspace/scripts"
DEST="$DEST_DIR/compose.yaml"
mkdir -p "$DEST_DIR"
if [ -f "$DEST" ] && cmp -s "$COMPOSE_SRC" "$DEST"; then
  echo "OK: $DEST is up to date"
else
  if [ -f "$DEST" ]; then
    cp "$DEST" "$DEST.bak"
    echo "Saved the previous file as $DEST.bak"
  fi
  cp "$COMPOSE_SRC" "$DEST"
  echo "Wrote $DEST"
fi

printf '\nDone. fleetest starts and stops the server on every run.\n'
