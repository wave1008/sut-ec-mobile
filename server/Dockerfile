# 前提: ホストで `./gradlew :server:installDist` 済み(イメージ内で Gradle を回さない=軽量化のため)。
# ルートコンテキストから build する。.dockerignore はホワイトリスト(下の2パス以外を送らない)。
# server/build.gradle.kts の出力の版(Java 25)と揃える。
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY server/build/install/server ./
COPY mock-server/images /app/mock-server/images
ENV PORT=8090
ENV IMAGES_DIR=/app/mock-server/images
EXPOSE 8090
CMD ["./bin/server"]
