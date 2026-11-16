FROM maven:3.9-eclipse-temurin-17 AS sources
WORKDIR /workspace
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src

FROM sources AS build
RUN mvn -B package -DskipTests

FROM alpine:3.22 AS tools
RUN apk add --no-cache bash curl jq
CMD ["bash"]

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/orm-platform-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
