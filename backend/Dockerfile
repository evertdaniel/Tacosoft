# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER appuser
ENV DB_URL="" \
    DB_USER="" \
    DB_PASSWORD="" \
    JWT_SECRET="" \
    SERVER_PORT=8080 \
    CORS_ORIGINS=""
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
