# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
# Cap build memory so a build on a small server (e.g. a Raspberry Pi) doesn't
# starve the services running next to it. Kotlin compiles inside the Gradle
# JVM instead of a separate daemon, so this one heap limit covers everything.
RUN printf '%s\n' \
    'org.gradle.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=384m' \
    'kotlin.compiler.execution.strategy=in-process' \
    'org.gradle.workers.max=2' > gradle.properties
# Resolve the Gradle distribution and dependencies in a cached layer
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar \
    && cp build/libs/*-SNAPSHOT.jar app.jar

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 app
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
