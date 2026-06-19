# ==========================================
# ETAPA 1: Constructor (Builder)
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ==========================================
# ETAPA 2: Producción (Runner)
# ==========================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
