FROM maven:3.9-eclipse-temurin-17 AS sources
WORKDIR /workspace
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src

FROM sources AS build
RUN --mount=type=cache,id=orm-platform-maven,target=/root/.m2/repository,sharing=locked \
    for attempt in 1 2 3; do \
        if mvn -B -U -ntp -Dmaven.artifact.threads=2 \
                -Daether.transport.http.retryHandler.count=5 package -DskipTests; then \
            exit 0; \
        fi; \
        if [ "$attempt" -lt 3 ]; then \
            echo "Maven build failed, retrying with the same dependency cache"; \
            sleep "$((attempt * 5))"; \
        fi; \
    done; \
    exit 1

FROM alpine:3.22 AS tools
RUN apk add --no-cache bash curl jq
CMD ["bash"]

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/orm-platform-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
