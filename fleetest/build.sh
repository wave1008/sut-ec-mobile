#!/bin/bash
# fleetest のクイックスタート用に、sut-ec-mobile の iOS(Simulator 用)と Android のアプリをビルドする。どこから実行してもよい:
#   bash ../sut-ec-mobile/fleetest/build.sh [ios|android](省略で両方)
# 最後の2行 "App (iOS): <パス>" / "App (Android): <パス>" は fleetest profile setup --app-path に渡すパス(AI が読む契約。書式を変えない)。
# JDK は prepare.sh が入れる Homebrew の openjdk@25 だけを使う(iOS のビルドも Xcode のビルドフェーズで Gradle を呼ぶので要る)。
# Android SDK が見つからなければ、引数なしのときは Android を飛ばす(android を明示したときは止める)。
set -euo pipefail

TARGET="${1:-all}"
case "$TARGET" in all|ios|android) ;; *) printf 'ERROR: unknown target: %s (use ios or android)\n' "$TARGET" >&2; exit 1;; esac
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# 非対話のシェルでは Homebrew の bin が PATH に無いことがある(このプロセスの中だけで足す)
export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"

fail() { printf 'ERROR: %s\n' "$1" >&2; exit 1; }

[ "$(uname -s)" = "Darwin" ] && [ "$(uname -m)" = "arm64" ] || fail "this needs an Apple Silicon Mac"
command -v brew >/dev/null 2>&1 || fail "Homebrew is not installed. Install it from https://brew.sh and run this again"
# openjdk@25 は keg-only で /usr/libexec/java_home に出ない(prepare.sh と同じ見方)
JAVA_HOME="$(brew --prefix)/opt/openjdk@25"
[ -x "$JAVA_HOME/bin/java" ] || fail "JDK 25 not found at $JAVA_HOME (run prepare.sh first, or: brew install openjdk@25)"
export JAVA_HOME
echo "JDK: $JAVA_HOME"

IOS_APP="$ROOT/build/ios-sim/Build/Products/Debug-iphonesimulator/iosApp.app"
APK="$ROOT/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
built_ios=""
built_android=""

if [ "$TARGET" != "android" ]; then
  command -v xcodebuild >/dev/null 2>&1 || fail "xcodebuild not found (install Xcode)"
  echo "Building the iOS app (Simulator) ..."
  (cd "$ROOT" && xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
    -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
    -derivedDataPath build/ios-sim CODE_SIGNING_ALLOWED=NO ARCHS=arm64 ONLY_ACTIVE_ARCH=YES \
    -quiet build)
  [ -d "$IOS_APP" ] || fail "the iOS build finished but $IOS_APP is missing"
  built_ios=1
fi

if [ "$TARGET" != "ios" ]; then
  # Gradle は local.properties の sdk.dir か ANDROID_HOME で SDK を探す(新しいクローンには local.properties が無い)
  sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
  [ -n "$sdk" ] || sdk="$(sed -n 's/^sdk\.dir=//p' "$ROOT/local.properties" 2>/dev/null || true)"
  [ -n "$sdk" ] || sdk="$HOME/Library/Android/sdk"
  if [ -d "$sdk/platform-tools" ]; then
    export ANDROID_HOME="$sdk"
    echo "Building the Android app ..."
    (cd "$ROOT" && ./gradlew --quiet :androidApp:assembleDebug)
    [ -f "$APK" ] || fail "the Android build finished but $APK is missing"
    built_android=1
  elif [ "$TARGET" = "android" ]; then
    fail "Android SDK not found (looked at ANDROID_HOME, local.properties and $sdk)"
  else
    echo "Skipped the Android app: Android SDK not found (looked at ANDROID_HOME, local.properties and $sdk)"
  fi
fi

echo
echo "Done."
[ -z "$built_ios" ] || echo "App (iOS): $IOS_APP"
[ -z "$built_android" ] || echo "App (Android): $APK"
