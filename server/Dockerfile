FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :server:installDist --no-daemon -Pcodingarena.android=false -Pcodingarena.apple=false

FROM eclipse-temurin:17-jre
RUN useradd --create-home --uid 10001 arena
USER arena
WORKDIR /app
COPY --from=build /workspace/server/build/install/server/ ./
EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]

