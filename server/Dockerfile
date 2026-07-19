# マルチステージ: gradle でビルド → JRE で実行。ルートコンテキストから build する(docker-compose.yml)。
FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/server/build/install/server ./
# 画像実体を同梱(IMAGES_DIR で参照)。
COPY --from=build /app/mock-server/images /app/mock-server/images
ENV PORT=8090
ENV IMAGES_DIR=/app/mock-server/images
EXPOSE 8090
CMD ["./bin/server"]
